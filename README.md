-----------------------------------------------------------------------------------------------------------------------------------------------------
5.7.2026.

- Kreirana aplikacija za opciju kada su dva igraca na istom uredjaju.
- Realizovana logika za racunanje 4 diska u nizu u svim pravcima (vertokalno, horizontalno, po glavnoj i po sporednoj dijagonali).
- U narednom koraku ce se realizovati serverski deo mrezne aplikacije.

* AI alat je koriscen za proveru funkcionalnosti pojedinih linija koda, zbog same Java sintakse.
* Kod je napisan u skladu sa primerima sa vezbi i predavanja. :)
-----------------------------------------------------------------------------------------------------------------------------------------------------
6.7.2026.

- Kreiran serverski deo aplikacije, po ugledu na prosli **projekat Menjaza**.
- U narednom koraku ce se realizovati klijentski deo mrezne aplikacije.

* AI alat je koriscen za proveru funkcionalnosti pojedinih linija koda, zbog same Java sintakse.
* Kod je napisan u skladu sa primerima sa vezbi i predavanja. :)
-----------------------------------------------------------------------------------------------------------------------------------------------------
7.7.2026.

- Dodata je jos jedna **klasa NetworkConnection**.
- Testiran serverski deo aplikacije sa Command prompt-om i emulatorom, radilo je ispravno.
- U narednom koraku ce se realizovati klijentski deo mrezne aplikacije, kao i vizuelni prikaz sa dve aktivnosti (dva prozora).

* AI alat je koriscen za proveru funkcionalnosti pojedinih linija koda, zbog same Java sintakse.
* Kod je napisan u skladu sa primerima sa vezbi i predavanja. :)
-----------------------------------------------------------------------------------------------------------------------------------------------------
8.7.2026.

- Dodata je jos jedna **aktivnost HomeScreen**, gde igrac unosi IP adresu i korisnicko ime, pa se nakon toga konektuje i bira iz liste sa kojim igracem zeli da pocne mec.
- Testirana aplikacija sa Command prompt-om i emulatorom, radilo je sve ispravno.

**- Tok igre:**
- Pokrene se server klasa ConnectFourServer, nakon toga u Command prompt-u se igrac konektuje pomocu _telnet localhost 4925_, i unese korisnicko ime.
- U Android studiju se pokrene emulator, pojavljuje se HomeScreen (prva aktivnost) u kojoj se unosi IP adresa i korisnicko ime, i bira se igrac iz liste.
- Nakon toga igrac iz Command prompt-a odgovara sa _RESPONSE;ACCEPT;UserNameOdProtivnika_, pa se oba igraca prebacuju na prozor sa praznom tablom - MainActivity (druga aktivnost), i igra moze da pocne.
- Iz Command prompt-a potez se odigrava ispisivanjem _TURN;broj_, dok igrac na emulatoru moze da klikne na odredjenu kolonu.
- Pobednik je onaj koji spoji 4 diska u nizu, potom igraci mogu da posalje upit za revansom.
- Ako oba igraca pristanu, tabla se prazni i igra pocinje iz pocetka, u suprotnom, igraci se vracaju na pocetni ekran (lobi - HomeScreen), i omogucava im se ponovni izbor protivnika.

* AI alat je koriscen za proveru funkcionalnosti pojedinih linija koda, zbog same Java sintakse.
* Kod je napisan u skladu sa primerima sa vezbi i predavanja. :)
