import SectionTitle from "@/components/ui/SectionTitle";

export default function MentionsLegalesPage() {
  return (
    <section className="py-20 md:py-28 bg-dark">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
        <SectionTitle
          title="Mentions legales"
          subtitle="Informations legales relatives au site SRPDesk."
          align="left"
          dark
        />

        <div className="space-y-10">
          <section>
            <h2 className="text-xl font-bold text-text-light mb-4">
              1. Editeur du site
            </h2>
            <p className="text-text-muted-dark leading-relaxed">
              Le site SRPDesk est edite par :<br />
              <strong className="text-text-light">SRPDesk SAS</strong>
              <br />
              Siege social : [Adresse a completer]
              <br />
              RCS : [Numero a completer]
              <br />
              Capital social : [Montant a completer]
              <br />
              Email : contact@srpdesk.com
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold text-text-light mb-4">
              2. Directeur de la publication
            </h2>
            <p className="text-text-muted-dark leading-relaxed">
              [Nom du directeur de la publication a completer]
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold text-text-light mb-4">
              3. Hebergement
            </h2>
            <p className="text-text-muted-dark leading-relaxed">
              Le site est heberge par :<br />
              [Nom de l&apos;hebergeur a completer]
              <br />
              [Adresse a completer]
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold text-text-light mb-4">
              4. Propriete intellectuelle
            </h2>
            <p className="text-text-muted-dark leading-relaxed">
              L&apos;ensemble du contenu du site SRPDesk (textes, graphismes,
              logo, icones, images, clips audio et video, logiciels) est la
              propriete exclusive de SRPDesk SAS ou de ses partenaires et est
              protege par les lois francaises et internationales relatives a la
              propriete intellectuelle. Toute reproduction, representation,
              modification, publication, adaptation de tout ou partie des
              elements du site, quel que soit le moyen ou le procede utilise,
              est interdite sauf autorisation ecrite prealable de SRPDesk SAS.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold text-text-light mb-4">
              5. Protection des donnees personnelles
            </h2>
            <p className="text-text-muted-dark leading-relaxed">
              Conformement au Reglement General sur la Protection des Donnees
              (RGPD) et a la loi Informatique et Libertes, vous disposez d&apos;un
              droit d&apos;acces, de rectification, de suppression et d&apos;opposition
              sur vos donnees personnelles. Pour exercer ces droits, contactez
              nous a : contact@srpdesk.com.
            </p>
            <p className="text-text-muted-dark leading-relaxed mt-4">
              SRPDesk ne collecte des donnees personnelles que dans le cadre
              des demandes de contact et de demonstration. Ces donnees ne sont
              jamais transmises a des tiers sans votre consentement.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold text-text-light mb-4">
              6. Cookies
            </h2>
            <p className="text-text-muted-dark leading-relaxed">
              Ce site utilise des cookies strictement necessaires a son
              fonctionnement. Aucun cookie de tracking publicitaire n&apos;est
              utilise. Vous pouvez configurer votre navigateur pour refuser les
              cookies.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold text-text-light mb-4">
              7. Limitation de responsabilite
            </h2>
            <p className="text-text-muted-dark leading-relaxed">
              SRPDesk SAS s&apos;efforce de fournir des informations aussi
              precises que possible. Toutefois, elle ne pourra etre tenue
              responsable des omissions, des inexactitudes et des carences dans
              la mise a jour, qu&apos;elles soient de son fait ou du fait des tiers
              partenaires qui lui fournissent ces informations.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-bold text-text-light mb-4">
              8. Droit applicable
            </h2>
            <p className="text-text-muted-dark leading-relaxed">
              Les presentes mentions legales sont regies par le droit francais.
              En cas de litige, les tribunaux francais seront seuls competents.
            </p>
          </section>
        </div>
      </div>
    </section>
  );
}
