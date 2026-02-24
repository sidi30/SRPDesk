import { useState } from 'react';
import { useApiKeys, useCreateApiKey, useRevokeApiKey } from '../hooks/useApiKeys';
import { Modal } from '../components/Modal';
import { getErrorMessage } from '../types';

const CI_TABS = ['GitHub Actions', 'GitLab CI', 'Script'] as const;

function githubActionsSnippet(apiUrl: string) {
  return `name: SBOM Upload to SRPDesk

on:
  push:
    branches: [main]
  release:
    types: [published]

jobs:
  sbom:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Generate SBOM (CycloneDX)
        uses: CycloneDX/gh-dotnet-generate-sbom@v1
        with:
          path: .
          output: sbom.cdx.json

      - name: Upload SBOM to SRPDesk
        run: |
          curl -sSf -X POST ${apiUrl}/integrations/ci/sbom \\
            -H "X-API-Key: \${{ secrets.SRPDESK_API_KEY }}" \\
            -F "file=@sbom.cdx.json" \\
            -F "productName=\${{ github.event.repository.name }}" \\
            -F "version=\${{ github.ref_name }}" \\
            -F "gitRef=\${{ github.sha }}"`;
}

function gitlabCiSnippet(apiUrl: string) {
  return `sbom-upload:
  stage: deploy
  image: curlimages/curl:latest
  script:
    - |
      curl -sSf -X POST ${apiUrl}/integrations/ci/sbom \\
        -H "X-API-Key: $SRPDESK_API_KEY" \\
        -F "file=@sbom.cdx.json" \\
        -F "productName=$CI_PROJECT_NAME" \\
        -F "version=$CI_COMMIT_TAG" \\
        -F "gitRef=$CI_COMMIT_SHA"
  rules:
    - if: $CI_COMMIT_TAG`;
}

function genericSnippet(apiUrl: string) {
  return `#!/bin/bash
# Upload SBOM to SRPDesk
# Required: SRPDESK_API_KEY environment variable

SRPDESK_URL="${apiUrl}"
PRODUCT_NAME="mon-produit"
VERSION="1.0.0"
SBOM_FILE="sbom.cdx.json"

curl -sSf -X POST "\${SRPDESK_URL}/integrations/ci/sbom" \\
  -H "X-API-Key: \${SRPDESK_API_KEY}" \\
  -F "file=@\${SBOM_FILE}" \\
  -F "productName=\${PRODUCT_NAME}" \\
  -F "version=\${VERSION}" \\
  -F "gitRef=\$(git rev-parse HEAD)"`;
}

export function IntegrationsPage() {
  const { data: apiKeys, isLoading } = useApiKeys();
  const createMutation = useCreateApiKey();
  const revokeMutation = useRevokeApiKey();

  const [showCreate, setShowCreate] = useState(false);
  const [keyName, setKeyName] = useState('');
  const [createdKey, setCreatedKey] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState<typeof CI_TABS[number]>('GitHub Actions');
  const [copied, setCopied] = useState(false);
  const [confirmRevoke, setConfirmRevoke] = useState<string | null>(null);

  const apiUrl = window.location.origin;

  const handleCreate = async () => {
    if (!keyName.trim()) return;
    setError('');
    try {
      const result = await createMutation.mutateAsync({ name: keyName.trim() });
      setCreatedKey(result.plainTextKey);
      setKeyName('');
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleRevoke = async (id: string) => {
    try {
      await revokeMutation.mutateAsync(id);
      setConfirmRevoke(null);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const snippets: Record<typeof CI_TABS[number], string> = {
    'GitHub Actions': githubActionsSnippet(apiUrl),
    'GitLab CI': gitlabCiSnippet(apiUrl),
    'Script': genericSnippet(apiUrl),
  };

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-900 mb-1">Integrations CI/CD</h1>
      <p className="text-gray-500 mb-8">Automatisez l'upload de vos SBOM depuis vos pipelines CI/CD</p>

      {/* ── API Keys Section ──────────────────────── */}
      <section className="mb-10">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-900">Cles API</h2>
          <button
            onClick={() => { setShowCreate(true); setCreatedKey(null); setError(''); }}
            className="px-4 py-2 bg-primary-600 text-white text-sm font-medium rounded-lg hover:bg-primary-700 transition-colors"
          >
            Creer une cle
          </button>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">{error}</div>
        )}

        {/* Created key banner */}
        {createdKey && (
          <div className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg">
            <p className="text-sm font-medium text-green-800 mb-2">
              Cle API creee avec succes. Copiez-la maintenant, elle ne sera plus affichee.
            </p>
            <div className="flex items-center gap-2">
              <code className="flex-1 p-2 bg-white border rounded text-sm font-mono break-all">{createdKey}</code>
              <button
                onClick={() => copyToClipboard(createdKey)}
                className="px-3 py-2 bg-green-600 text-white text-sm rounded hover:bg-green-700 transition-colors"
              >
                {copied ? 'Copie !' : 'Copier'}
              </button>
            </div>
          </div>
        )}

        {/* Create modal */}
        <Modal open={showCreate && !createdKey} onClose={() => setShowCreate(false)} maxWidth="max-w-md">
            <div className="p-6">
              <h3 className="text-lg font-semibold mb-4">Nouvelle cle API</h3>
              <label className="block text-sm font-medium text-gray-700 mb-1">Nom</label>
              <input
                type="text"
                value={keyName}
                onChange={e => setKeyName(e.target.value)}
                placeholder="Ex: GitLab CI - mon-projet"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm mb-4 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                autoFocus
              />
              <div className="flex justify-end gap-2">
                <button
                  onClick={() => setShowCreate(false)}
                  className="px-4 py-2 text-sm text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
                >
                  Annuler
                </button>
                <button
                  onClick={handleCreate}
                  disabled={createMutation.isPending || !keyName.trim()}
                  className="px-4 py-2 text-sm text-white bg-primary-600 rounded-lg hover:bg-primary-700 disabled:opacity-50"
                >
                  {createMutation.isPending ? 'Creation...' : 'Creer'}
                </button>
              </div>
            </div>
        </Modal>

        {/* Keys table */}
        {isLoading ? (
          <p className="text-gray-500 text-sm">Chargement...</p>
        ) : (
          <div className="border border-gray-200 rounded-lg overflow-hidden">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nom</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Prefixe</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Statut</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Derniere utilisation</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Creee le</th>
                  <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {(!apiKeys || apiKeys.length === 0) ? (
                  <tr>
                    <td colSpan={6} className="px-4 py-8 text-center text-sm text-gray-500">
                      Aucune cle API. Creez-en une pour commencer.
                    </td>
                  </tr>
                ) : apiKeys.map(key => (
                  <tr key={key.id} className={key.revoked ? 'opacity-50' : ''}>
                    <td className="px-4 py-3 text-sm font-medium text-gray-900">{key.name}</td>
                    <td className="px-4 py-3 text-sm text-gray-500 font-mono">{key.keyPrefix}...</td>
                    <td className="px-4 py-3 text-sm">
                      {key.revoked ? (
                        <span className="inline-flex px-2 py-0.5 text-xs font-medium rounded-full bg-red-100 text-red-800">Revoquee</span>
                      ) : (
                        <span className="inline-flex px-2 py-0.5 text-xs font-medium rounded-full bg-green-100 text-green-800">Active</span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-500">
                      {key.lastUsedAt ? new Date(key.lastUsedAt).toLocaleString('fr-FR') : 'Jamais'}
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-500">
                      {new Date(key.createdAt).toLocaleDateString('fr-FR')}
                    </td>
                    <td className="px-4 py-3 text-right">
                      {!key.revoked && (
                        confirmRevoke === key.id ? (
                          <div className="flex items-center justify-end gap-2">
                            <span className="text-xs text-gray-500">Confirmer ?</span>
                            <button
                              onClick={() => handleRevoke(key.id)}
                              className="px-2 py-1 text-xs text-white bg-red-600 rounded hover:bg-red-700"
                            >
                              Revoquer
                            </button>
                            <button
                              onClick={() => setConfirmRevoke(null)}
                              className="px-2 py-1 text-xs text-gray-600 bg-gray-100 rounded hover:bg-gray-200"
                            >
                              Annuler
                            </button>
                          </div>
                        ) : (
                          <button
                            onClick={() => setConfirmRevoke(key.id)}
                            className="text-sm text-red-600 hover:text-red-800"
                          >
                            Revoquer
                          </button>
                        )
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* ── CI/CD Snippets Section ────────────────── */}
      <section>
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Configuration CI/CD</h2>
        <p className="text-sm text-gray-500 mb-4">
          Copiez le snippet correspondant a votre CI et ajoutez votre cle API en tant que secret d'environnement.
        </p>

        {/* Tabs */}
        <div className="flex border-b border-gray-200 mb-4">
          {CI_TABS.map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
                activeTab === tab
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              {tab}
            </button>
          ))}
        </div>

        {/* Snippet */}
        <div className="relative">
          <button
            onClick={() => copyToClipboard(snippets[activeTab])}
            className="absolute top-2 right-2 px-3 py-1 text-xs bg-gray-700 text-white rounded hover:bg-gray-600 transition-colors"
          >
            {copied ? 'Copie !' : 'Copier'}
          </button>
          <pre className="p-4 bg-gray-900 text-gray-100 rounded-lg text-sm overflow-x-auto leading-relaxed">
            <code>{snippets[activeTab]}</code>
          </pre>
        </div>

        <div className="mt-4 p-4 bg-blue-50 border border-blue-200 rounded-lg">
          <p className="text-sm text-blue-800">
            <strong>Secret requis :</strong> Ajoutez la variable <code className="bg-blue-100 px-1 rounded">SRPDESK_API_KEY</code> dans
            les secrets de votre projet CI/CD avec la valeur de la cle API generee ci-dessus.
          </p>
        </div>
      </section>
    </div>
  );
}
