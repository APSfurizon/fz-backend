<!DOCTYPE html>
<html lang="it">

<head>
    <meta charset="utf-8" />
    <title>Richiesta Ammissione Socio - Furizon</title>
    <style>
        * {
            box-sizing: border-box;
        }

        body {
            background-color: #e0e0e0;
            margin: 0;
            padding: 0;
        }

        .page {
            width: 210mm;
            min-height: 297mm;
            padding: 15mm;
            margin: 20px auto;
            background: white;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
            color: black;
        }

        @media print {
            body {
                background: white;
                margin: 0;
            }

            .page {
                margin: 0;
                padding: 10mm;
                box-shadow: none;
                width: 100%;
                min-height: 0;
            }

            @page {
                size: A4;
                margin: 5mm 0;
            }
        }

        .form-line {
            display: flex;
            align-items: flex-end;
            margin-bottom: 12px;
        }

        .label {
            font-size: 10pt;
            font-family: Arial, sans-serif;
            white-space: nowrap;
            color: black;
        }

        .value {
            font-size: 11pt;
            font-family: Arial, sans-serif;
            font-weight: bold;
            border-bottom: 1.5px solid black;
            padding-left: 5px;
            padding-bottom: 1px;
            line-height: 1.1;
            color: black;
        }

        .legal-box {
            border: 1px solid black;
            padding: 6px 8px;
            font-family: Arial, sans-serif;
            font-size: 8pt;
            text-align: justify;
            line-height: 1.35;
            color: black;
        }

        .section-title {
            text-align: center;
            font-family: Arial, sans-serif;
            font-size: 11pt;
            margin-top: 15px;
            margin-bottom: 5px;
            color: black;
        }

        .legal-text {
            font-family: Arial, sans-serif;
            font-size: 8pt;
            text-align: justify;
            line-height: 1.25;
            margin-bottom: 15px;
            margin-top: 5px;
            color: black;
        }
    </style>
</head>

<body>

    <div class="page">

        <!-- Intestazione con Logo SVG e Indirizzo -->
        <div style="display: flex; align-items: flex-start; margin-bottom: 20px;">
            <div style="width: 35%; padding-top: 5px;">
                <img src="https://furpanel.furizon.net/images/logo-light.png"
                    style="max-height: 55px; max-width: 100%;">
            </div>
            <div
                style="width: 65%; padding-left: 20px; font-family: Arial, sans-serif; font-size: 9pt; line-height: 1.4; color: black;">
                <strong style="font-size: 10.5pt;">APS Furizon</strong><br><br>
                Via G. Turri 12<br>
                42121 Reggio nell'Emilia (RE)<br>
                - Codice Fiscale 91181650358<br>
                Tel.3456679134 - e-mail. info@furizon.net - https://furizon.net
            </div>
        </div>

        <!-- Barra Centrale Titolo -->
        <div
            style="position: relative; border-top: 2px solid black; border-bottom: 2px solid black; padding: 6px 0; margin: 15px 0;">
            <div
                style="font-size: 14.5pt; font-family: Arial, sans-serif; font-weight: bold; text-align: center; color: black; letter-spacing: 0.5px;">
                RICHIESTA DI AMMISSIONE A SOCIO</div>
            <div
                style="position: absolute; right: 0; top: 50%; transform: translateY(-50%); font-size: 9pt; font-family: Arial, sans-serif; text-align: right; line-height: 1.25; color: black;">
                <strong>Tess: {tessera}</strong>
            </div>
        </div>

        <!-- Modulo Dati - Formattato in layout griglia -->

        <!-- Riga 1: Il sottoscritto... C.F. -->
        <div class="form-line" style="margin-bottom: 22px;">
            <div style="width: 65%; display: flex; align-items: flex-end;">
                <div class="label" style="position: relative; width: 90px;">
                    Il/La
                    <div style="position: absolute; top: 100%; left: 0;">sottoscritto/a</div>
                </div>
                <div class="value" style="flex-grow: 1;">{nome}</div>
            </div>
            <div style="width: 35%; display: flex; align-items: flex-end; padding-left: 15px;">
                <div class="label" style="width: 35px;">C.F.</div>
                <div class="value" style="flex-grow: 1;">{cf}</div>
            </div>
        </div>

        <!-- Riga 2: nato a... il... cittadinanza -->
        <div class="form-line" style="margin-top: 15px;">
            <div style="width: 45%; display: flex; align-items: flex-end;">
                <div class="label" style="width: 90px;">nato/a a</div>
                <div class="value" style="flex-grow: 1;">{nato_a}</div>
            </div>
            <div style="width: 25%; display: flex; align-items: flex-end; padding-left: 15px;">
                <div class="label" style="width: 15px; margin-right: 5px;">il</div>
                <div class="value" style="flex-grow: 1; text-align: center;">{data_nascita}</div>
            </div>
            <div style="width: 30%; display: flex; align-items: flex-end; padding-left: 15px;">
                <div class="label" style="margin-right: 5px;">cittadinanza</div>
                <div class="value" style="flex-grow: 1;">{cittadinanza}</div>
            </div>
        </div>

        <!-- Riga 3: residente a... prov -->
        <div class="form-line">
            <div style="width: 70%; display: flex; align-items: flex-end;">
                <div class="label" style="width: 90px;">residente a</div>
                <div class="value" style="flex-grow: 1;">{residente_a}</div>
            </div>
            <div style="width: 30%; display: flex; align-items: flex-end; padding-left: 15px;">
                <div class="label" style="width: 35px; margin-right: 5px;">prov.</div>
                <div class="value" style="flex-grow: 1;">{prov}</div>
            </div>
        </div>

        <!-- Riga 4: in via... cap -->
        <div class="form-line">
            <div style="width: 70%; display: flex; align-items: flex-end;">
                <div class="label" style="width: 90px;">in via/piazza</div>
                <div class="value" style="flex-grow: 1;">{via}</div>
            </div>
            <div style="width: 30%; display: flex; align-items: flex-end; padding-left: 15px;">
                <div class="label" style="width: 35px; margin-right: 5px;">cap</div>
                <div class="value" style="flex-grow: 1;">{cap}</div>
            </div>
        </div>

        <!-- Riga 5: E-Mail... tel -->
        <div class="form-line">
            <div style="width: 70%; display: flex; align-items: flex-end;">
                <div class="label" style="width: 90px;">E-Mail</div>
                <div class="value" style="flex-grow: 1;">{email}</div>
            </div>
            <div style="width: 30%; display: flex; align-items: flex-end; padding-left: 15px;">
                <div class="label" style="width: 35px; margin-right: 5px;">tel.</div>
                <div class="value" style="flex-grow: 1;">{tel}</div>
            </div>
        </div>

        <!-- Sezione: Richiesta Ammissione -->
        <div class="section-title" style="margin-top: 20px;">CHIEDE DI ESSERE AMMESSO</div>

        <div class="legal-box">
            quale socio della presente Associazione, per lo svolgimento e il raggiungimento degli scopi primari della
            stessa, attenendosi allo statuto sociale di cui dichiara di aver preso visione ed alle deliberazioni degli
            organi sociali, ed impegnandosi a corrispondere la quota sociale fissata annualmente dal Consiglio
            Direttivo. Dichiara, altresì, di aver preso visione dello statuto e di accettarlo integralmente.
        </div>

        <div style="display: flex; justify-content: space-around; margin-top: 30px; margin-bottom: 20px;">
            <div style="width: 40%; text-align: center; font-family: Arial, sans-serif; font-size: 11pt; color: black;">
                Data
                <div style="border-bottom: 1.5px solid black; margin-top: 5px; height: 25px; font-weight: bold; display: flex; align-items: flex-end; justify-content: center; padding-bottom: 2px;">
                    {data}
                </div>
            </div>
            <div style="width: 40%; text-align: center; font-family: Arial, sans-serif; font-size: 11pt; color: black;">
                Firma
                <div style="border-bottom: 1.5px solid black; margin-top: 5px; height: 25px; display: flex; align-items: flex-end; justify-content: center; padding-bottom: 2px;">
                </div>
            </div>
        </div>

        <!-- Divisorio tra le due sezioni -->
        <div style="border-top: 2px solid black; margin: 30px 0 20px 0;"></div>

        <!-- Sezione: Consenso -->
        <div class="section-title" style="font-weight: bold; margin-top: 10px;">CONSENSO AL TRATTAMENTO DEI DATI
            PERSONALI</div>
        <div class="legal-text">
            Ricevuta l’informativa sull’utilizzazione dei miei dati personali ai sensi dell’art.13 del Decreto
            Legislativo n.196/2003 e dell’articolo 13 del Regolamento UE n. 2016/679 consento al loro trattamento nella
            misura necessaria per il perseguimento degli scopi statutari. Il Titolare del trattamento è l’Associazione
            di Promozione Sociale "Furizon" nella persona del suo Presidente in carica con ufficio nella sede legale
            della stessa. Il trattamento dei dati sarà svolto in maniera sia manuale che automatizzata, nel rispetto di
            quanto indicato nella normativa di riferimento, ad opera di soggetti appositamente incaricati. Consento
            anche che i dati riguardanti l’iscrizione siano comunicati agli enti con cui l’associazione collabora e da
            questi trattati nella misura necessaria all’adempimento di obblighi previsti dalla legge e dalle norme
            statutarie. Acconsento inoltre alla conservazione dei dati "sensibili" di cui all'art. 4 comma 1 lett. d),
            nonché art. 26 del D.lgs.196/2003 e degli articoli 9 e 10 del Regolamento UE n. 2016/679, vale a dire i dati
            "idonei a rivelare l'origine razziale ed etnica, le convinzioni religiose, filosofiche o di altro genere, le
            opinioni politiche, l'adesione a partiti, sindacati, associazioni od organizzazioni a carattere religioso,
            filosofico, politico o sindacale, nonché i dati personali idonei a rivelare lo stato di salute e la vita
            sessuale". I dati saranno conservati per il periodo necessario al conseguimento delle finalità per le quali
            sono raccolti. Il conferimento dei dati è obbligatorio, pena l’impossibilità di proseguire il rapporto con
            l’Associazione APS Furizon.
        </div>

        <!-- Sezione: Rilascio Immagini -->
        <div class="section-title" style="font-weight: bold; margin-top: 25px;">AUTORIZZAZIONE ALL'UTILIZZO
            DELL'IMMAGINE</div>
        <div class="legal-text">
            Autorizzo la pubblicazione, anche ai sensi dell’art. 10 c.c., nonché degli artt. 96 e 97 legge 633/1941,
            delle fotografie e riprese video che ritraggono la mia immagine effettuate dallo Staff di Furizon, senza
            limiti di tempo, nel mondo intero, con ogni mezzo e per qualsiasi finalità, purché non pregiudizievoli per
            il decoro, l’onore, la reputazione, nonché la dignità della mia persona.
        </div>

        <!-- Blocco Firme Finale -->
        <div style="display: flex; justify-content: space-around; margin-top: 35px;">
            <div style="width: 40%; text-align: center; font-family: Arial, sans-serif; font-size: 11pt; color: black;">
                Data
                <div style="border-bottom: 1.5px solid black; margin-top: 5px; height: 25px; font-weight: bold; display: flex; align-items: flex-end; justify-content: center; padding-bottom: 2px;">
                    {data}
                </div>
            </div>
            <div style="width: 40%; text-align: center; font-family: Arial, sans-serif; font-size: 11pt; color: black;">
                Firma
                <div style="border-bottom: 1.5px solid black; margin-top: 5px; height: 25px; display: flex; align-items: flex-end; justify-content: center; padding-bottom: 2px;">
                </div>
            </div>
        </div>

    </div>

</body>

</html>