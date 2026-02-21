import { useState } from 'react';
import { useProducts } from '../hooks/useProducts';
import { useParseQuestionnaire, useFillQuestionnaire } from '../hooks/useAi';
import { FileUpload } from '../components/FileUpload';
import { FR } from '../i18n/fr';
import type { AiJobResponse } from '../types';
import { getErrorMessage } from '../types';

export function AiQuestionnairePage() {
  const { data: products } = useProducts();
  const parseQuestionnaire = useParseQuestionnaire();
  const fillQuestionnaire = useFillQuestionnaire();
  const [parsedText, setParsedText] = useState<string | null>(null);
  const [selectedProduct, setSelectedProduct] = useState('');
  const [result, setResult] = useState<AiJobResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const t = FR.ai;

  const handleUpload = (file: File) => {
    setError(null);
    setParsedText(null);
    setResult(null);
    parseQuestionnaire.mutate(file, {
      onSuccess: (text) => setParsedText(text),
      onError: (err: unknown) => setError(getErrorMessage(err, t.error)),
    });
  };

  const handleFill = () => {
    if (!parsedText) return;
    setError(null);
    setResult(null);
    fillQuestionnaire.mutate(
      { questionnaireText: parsedText, productId: selectedProduct || undefined },
      {
        onSuccess: (data) => setResult(data),
        onError: (err: unknown) => setError(getErrorMessage(err, t.error)),
      }
    );
  };

  const handleExportJson = () => {
    if (!result?.artifacts?.[0]) return;
    const blob = new Blob([JSON.stringify(result.artifacts[0].contentJson, null, 2)], {
      type: 'application/json',
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'questionnaire-answers.json';
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleExportCsv = () => {
    if (!result?.artifacts?.[0]) return;
    const answers = result.artifacts[0].contentJson as Array<Record<string, unknown>>;
    if (!Array.isArray(answers)) return;
    const lines = ['question_id;question;answer;confidence'];
    for (const a of answers) {
      const q = String(a.question || '').replace(/;/g, ',').replace(/\n/g, ' ');
      const ans = String(a.answer || '').replace(/;/g, ',').replace(/\n/g, ' ');
      lines.push(`${a.question_id};${q};${ans};${a.confidence}`);
    }
    const blob = new Blob([lines.join('\n')], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'questionnaire-answers.csv';
    a.click();
    URL.revokeObjectURL(url);
  };

  const answers = result?.artifacts?.[0]?.contentJson as Array<Record<string, unknown>> | undefined;

  const confidenceColor = (c: string) => {
    switch (c) {
      case 'HIGH': return 'bg-green-100 text-green-800';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800';
      case 'LOW': return 'bg-orange-100 text-orange-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">{t.questionnaire}</h1>
        <p className="mt-1 text-sm text-gray-500">{t.draftWarning}</p>
      </div>

      {/* Warning */}
      <div className="mb-6 p-3 bg-amber-50 border border-amber-200 rounded-lg text-sm text-amber-800 flex items-start gap-2">
        <svg className="w-5 h-5 shrink-0 mt-0.5 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
        {t.draftWarning}
      </div>

      {/* Step 1: Upload */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h3 className="text-sm font-semibold text-gray-700 mb-3">{t.uploadFile}</h3>
        <p className="text-xs text-gray-500 mb-3">{t.uploadHint}</p>
        <FileUpload
          onUpload={handleUpload}
          isLoading={parseQuestionnaire.isPending}
          accept=".xlsx,.docx,.txt,.csv"
        />
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">{error}</div>
      )}

      {/* Step 2: Preview + Generate */}
      {parsedText && (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <div className="flex justify-between items-center mb-3">
            <h3 className="text-sm font-semibold text-gray-700">{t.preview}</h3>
            <span className="text-xs text-gray-400">{parsedText.split('\n').length} lignes</span>
          </div>
          <pre className="bg-gray-50 border border-gray-200 rounded-lg p-4 text-xs whitespace-pre-wrap max-h-[200px] overflow-y-auto mb-4">
            {parsedText.slice(0, 3000)}{parsedText.length > 3000 ? '\n...' : ''}
          </pre>

          <div className="flex gap-4 items-end">
            <div className="flex-1">
              <label className="block text-xs font-medium text-gray-500 mb-1">{t.selectProduct}</label>
              <select
                value={selectedProduct}
                onChange={(e) => setSelectedProduct(e.target.value)}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="">-- Tous --</option>
                {products?.map((p) => (
                  <option key={p.id} value={p.id}>{p.name}</option>
                ))}
              </select>
            </div>
            <button
              onClick={handleFill}
              disabled={fillQuestionnaire.isPending}
              className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 disabled:opacity-50"
            >
              {fillQuestionnaire.isPending ? t.generating : t.fillAnswers}
            </button>
          </div>
        </div>
      )}

      {/* Step 3: Results */}
      {answers && Array.isArray(answers) && (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="p-4 flex justify-between items-center border-b border-gray-200">
            <h3 className="text-sm font-semibold text-gray-700">{t.answers} ({answers.length})</h3>
            <div className="flex gap-2">
              <button
                onClick={handleExportJson}
                className="px-3 py-1.5 text-xs font-medium text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-100"
              >
                {t.export} JSON
              </button>
              <button
                onClick={handleExportCsv}
                className="px-3 py-1.5 text-xs font-medium text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-100"
              >
                {t.export} CSV
              </button>
            </div>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase w-[60px]">#</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">{t.questions}</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">{t.answers}</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase w-[100px]">{t.confidence}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {answers.map((qa, i) => (
                  <tr key={String(qa.question_id || i)} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-xs text-gray-400 font-mono">{String(qa.question_id)}</td>
                    <td className="px-4 py-3 text-sm text-gray-900">{String(qa.question)}</td>
                    <td className="px-4 py-3 text-sm text-gray-700">{String(qa.answer)}</td>
                    <td className="px-4 py-3">
                      <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${confidenceColor(String(qa.confidence))}`}>
                        {String(qa.confidence)}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
