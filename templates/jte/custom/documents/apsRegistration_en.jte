@param String cardNo
@param String fullname
@param String fiscalCode
@param String birthCity
@param String birthDay
@param String city
@param String region
@param String address
@param String zipCode
@param String email
@param String phoneNumber
@param String todayDate

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <title>Application for membership - Furizon</title>
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
            <img src="https://furpanel.furizon.net/images/logo-light.png" style="max-height: 55px; max-width: 100%;">
        </div>
        <div style="width: 65%; padding-left: 20px; font-family: Arial, sans-serif; font-size: 9pt; line-height: 1.4; color: black;">
            <strong style="font-size: 10.5pt;">APS Furizon</strong><br><br>
            Via G. Turri 12<br>
            42121 Reggio nell'Emilia (RE)<br>
            - Fiscal code 91181650358<br>
            Tel.3456679134 - e-mail. <!--email_off-->info@furizon.net<!--/email_off--> - https://furizon.net
        </div>
    </div>

    <!-- Barra Centrale Titolo -->
    <div style="position: relative; border-top: 2px solid black; border-bottom: 2px solid black; padding: 6px 0; margin: 15px 0;">
        <div style="font-size: 14.5pt; font-family: Arial, sans-serif; font-weight: bold; text-align: center; color: black; letter-spacing: 0.5px;">
            APPLICATION FOR MEMBERSHIP
        </div>
        <div style="position: absolute; right: 0; top: 50%; transform: translateY(-50%); font-size: 9pt; font-family: Arial, sans-serif; text-align: right; line-height: 1.25; color: black;">
            <strong>Card: ${cardNo}</strong>
        </div>
    </div>

    <!-- Modulo Dati - Formattato in layout griglia -->

    <!-- Riga 1: Il sottoscritto... C.F. -->
    <div class="form-line" style="margin-bottom: 22px;">
        <div style="width: 63%; display: flex; align-items: flex-end;">
            <div class="label" style="position: relative; width: 90px;">
                The
                <div style="position: absolute; top: 100%; left: 0;">undersigned</div>
            </div>
            <div class="value" style="flex-grow: 1;">${fullname}</div>
        </div>
        <div style="width: 37%; display: flex; align-items: flex-end; padding-left: 15px;">
            <div class="label" style="width: 70px;">fiscal code</div>
            <div class="value" style="flex-grow: 1;">${fiscalCode}</div>
        </div>
    </div>

    <!-- Riga 2: nato a... il... cittadinanza -->
    <div class="form-line" style="margin-top: 15px;">
        <div style="width: 45%; display: flex; align-items: flex-end;">
            <div class="label" style="width: 90px;">born in</div>
            <div class="value" style="flex-grow: 1;">${birthCity}</div>
        </div>
        <div style="width: 20%; display: flex; align-items: flex-end; padding-left: 15px;">
            <div class="label" style="width: 15px; margin-right: 5px;">on</div>
            <div class="value" style="flex-grow: 1; text-align: center;">${birthDay}</div>
        </div>
        <div style="width: 35%; display: flex; align-items: flex-end; padding-left: 15px;">
            <div class="label" style="margin-right: 5px;">Citizenship</div>
            <div class="value" style="flex-grow: 1;"></div>
        </div>
    </div>

    <!-- Riga 3: residente a... prov -->
    <div class="form-line">
        <div style="width: 65%; display: flex; align-items: flex-end;">
            <div class="label" style="width: 90px;">Resident in</div>
            <div class="value" style="flex-grow: 1;">${city}</div>
        </div>
        <div style="width: 35%; display: flex; align-items: flex-end; padding-left: 15px;">
            <div class="label" style="width: 60px; margin-right: 5px;">state</div>
            <div class="value" style="flex-grow: 1;">${region}</div>
        </div>
    </div>

    <!-- Riga 4: in via... cap -->
    <div class="form-line">
        <div style="width: 65%; display: flex; align-items: flex-end;">
            <div class="label" style="width: 90px;">address</div>
            <div class="value" style="flex-grow: 1;">${address}</div>
        </div>
        <div style="width: 35%; display: flex; align-items: flex-end; padding-left: 15px;">
            <div class="label" style="width: 60px; margin-right: 5px;">zip code</div>
            <div class="value" style="flex-grow: 1;">${zipCode}</div>
        </div>
    </div>

    <!-- Riga 5: E-Mail... tel -->
    <div class="form-line">
        <div style="width: 65%; display: flex; align-items: flex-end;">
            <div class="label" style="width: 90px;">E-Mail</div>
            <div class="value" style="flex-grow: 1;"><!--email_off-->${email}<!--/email_off--></div>
        </div>
        <div style="width: 35%; display: flex; align-items: flex-end; padding-left: 15px;">
            <div class="label" style="width: 60px; margin-right: 5px;">phone no.</div>
            <div class="value" style="flex-grow: 1;">${phoneNumber}</div>
        </div>
    </div>

    <!-- Sezione: Richiesta Ammissione -->
    <div class="section-title" style="margin-top: 20px;">ASKS TO BE ADMITTED</div>

    <div class="legal-box">
        as a member of this Association, for the performance and achievement of its primary purposes, complying with the Articles of Association of which he declares to
        have read and the resolutions of the corporate bodies, and undertaking to pay the membership fee set annually by the Board of Directors. He also declares that he
        has read the statute and accepts it in full.
    </div>

    <div style="display: flex; justify-content: space-around; margin-top: 30px; margin-bottom: 20px;">
        <div style="width: 40%; text-align: center; font-family: Arial, sans-serif; font-size: 11pt; color: black;">
            Date
            <div style="border-bottom: 1.5px solid black; margin-top: 5px; height: 25px; font-weight: bold; display: flex; align-items: flex-end; justify-content: center; padding-bottom: 2px;">
                ${todayDate}
            </div>
        </div>
        <div style="width: 40%; text-align: center; font-family: Arial, sans-serif; font-size: 11pt; color: black;">
            Signature
            <div style="border-bottom: 1.5px solid black; margin-top: 5px; height: 25px; display: flex; align-items: flex-end; justify-content: center; padding-bottom: 2px;">
            </div>
        </div>
    </div>

    <!-- Divisorio tra le due sezioni -->
    <div style="border-top: 2px solid black; margin: 30px 0 20px 0;"></div>

    <!-- Sezione: Consenso -->
    <div class="section-title" style="font-weight: bold; margin-top: 10px;">CONSENT TO THE PROCESSING OF PERSONAL DATA</div>
    <div class="legal-text">
        Having received the information on the use of my personal data pursuant to Article 13 of Legislative Decree No. 196/2003 and Article 13 of EU Regulation No.
        2016/679, I consent to their processing to the extent necessary for the pursuit of the statutory purposes. The Data Controller is the "Furizon" Social Promotion
        Association in the person of its President in office at the registered office of the same. The processing of data will be carried out both manually and automatically, in
        compliance with the provisions of the reference legislation, by specially appointed subjects. I also consent that the data concerning registration be communicated to
        the entities with which the association collaborates and processed by them to the extent necessary for the fulfillment of obligations provided for by law and by the
        statutory regulations. I also consent to the storage of "sensitive" data pursuant to art. 4 paragraph 1 letter d), as well as art. 26 of Legislative Decree 196/2003 and
        articles 9 and 10 of EU Regulation no. 2016/679, i.e. data "suitable for revealing racial and ethnic origin, religious, philosophical or other beliefs, political opinions,
        membership of parties, trade unions, associations or organizations of a religious, philosophical, philosophical nature,  political or trade union, as well as personal data
        suitable for revealing the state of health and sex life". The data will be stored for the period necessary to achieve the purposes for which they are collected. The
        provision of data is mandatory, otherwise it will be impossible to continue the relationship with the APS Furizon Association.
    </div>

    <!-- Sezione: Rilascio Immagini -->
    <div class="section-title" style="font-weight: bold; margin-top: 25px;">PERMISSION AND RELEASE FORM FOR IMAGES</div>
    <div class="legal-text">
        I authorize the publication, also pursuant to art. 10 of the Italian Civil Code, as well as art. 96 and 97 of law 633/1941, of photographs and video footage depicting my
        image taken by the Furizon Staff, without time limits, throughout the world, by any means and for any purpose, as long as they are not detrimental to the decorum, honor,
        reputation, as well as dignity of my person.
    </div>

    <!-- Blocco Firme Finale -->
    <div style="display: flex; justify-content: space-around; margin-top: 35px;">
        <div style="width: 40%; text-align: center; font-family: Arial, sans-serif; font-size: 11pt; color: black;">
            Date
            <div style="border-bottom: 1.5px solid black; margin-top: 5px; height: 25px; font-weight: bold; display: flex; align-items: flex-end; justify-content: center; padding-bottom: 2px;">
                ${todayDate}
            </div>
        </div>
        <div style="width: 40%; text-align: center; font-family: Arial, sans-serif; font-size: 11pt; color: black;">
            Signature
            <div style="border-bottom: 1.5px solid black; margin-top: 5px; height: 25px; display: flex; align-items: flex-end; justify-content: center; padding-bottom: 2px;">
            </div>
        </div>
    </div>

</div>
<script>window.print();</script>
</body>

</html>