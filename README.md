# Transport Route Finder (Tražilac Transportnih Ruta)

Ovo je Java desktop aplikacija razvijena korišćenjem Swing biblioteke i WindowsBuilder-a, dizajnirana za pronalaženje optimalnih ruta putovanja unutar simulirane transportne mreže. Aplikacija omogućava korisnicima da pretražuju najefikasnije rute koristeći kombinaciju autobuskog i željezničkog prevoza, optimizujući putovanje prema vremenu, cijeni ili broju presjedanja.

## 🌟 Mogućnosti

* **Generisanje transportne mreže:** Dinamičko generisanje države kao `n x m` matrice gradova, gde svaki grad ima autobusku i željezničku stanicu sa definisanim polascima. Podaci se generišu i čuvaju u JSON formatu.
* **Parsiranje i mapiranje podataka:** Učitavanje i parsiranje transportnih podataka iz JSON fajla u objektno-orijentisanu strukturu.
* **Izgradnja grafa:** Izgradnja usmjerenog grafa transportne mreže, gde su stanice čvorovi, a polasci i presjedanja grane.
* **Optimalno pronalaženje rute:** Implementacija Dijkstra algoritma za pronalaženje najkraće rute između dvije odabrane stanice.
    * **Optimizacija po vremenu:** Pronalazi najbržu rutu.
    * **Optimizacija po cijeni:** Pronalazi najjeftiniju rutu.
    * **Optimizacija po broju presjedanja:** Pronalazi rutu sa najmanjim brojem presjedanja.
* **Interaktivni GUI:** Intuitivni grafički korisnički interfejs zasnovan na Swing-u za unos parametara, vizualizaciju mape i prikaz rezultata.
* **Vizualizacija rute na mapi:** Prikaz generisane mape gradova sa istaknutom pronađenom optimalnom rutom.
* **Kupovina karata:** Funkcionalnost za kupovinu odabrane rute, generisanje računa u tekstualnom fajlu i čuvanje u namjenski folder `racuni`.
* **Statistika prodaje:** Prikaz ukupnog broja prodatih karata i ukupnog prihoda od prodaje, učitanih pri pokretanju aplikacije.
* **Prikaz dodatnih ruta:** Mogućnost prikaza top 5 ruta (trenutno prikazuje optimalnu rutu kao primjer, za stvarnu implementaciju top 5 ruta potreban je napredniji algoritam poput K-najkraćih puteva).

## 🚀 Tehnologije

* **Jezik:** Java
* **GUI:** Swing (sa WindowsBuilder-om za dizajn)
* **Strukture podataka/Algoritmi:** Grafovi, Dijkstra algoritam
* **Serijalizacija/deserijalizacija:** JSON (korišćenjem internog parsera i generatora)

## 🏗️ Struktura projekta

Projekat je organizovan u sljedeće pakete:

* `main`: Glavna klasa za pokretanje aplikacije (`Main.java`).
* `model`: Sadrži klase koje predstavljaju entitete transportne mreže (npr. `Station`, `Departure`, `TransportData`).
* `graph`: Sadrži implementaciju grafa (`Graph.java`), čvorova (`Node`), grana (`Edge`) i logiku za Dijkstra algoritam.
* `util`: Pomoćne klase za generisanje JSON podataka (`TransportDataGenerator`), parsiranje JSON-a (`SimpleJsonParser`), mapiranje parsiranih podataka (`TransportDataMapper`) i upravljanje računima (`ReceiptManager`).
* `gui`: Sadrži klase za grafički korisnički interfejs (`MainWindow.java`, `MapPanel.java`, `AdditionalRoutesWindow.java`).

## ⚙️ Pokretanje projekta

1.  **Klonirajte repozitorijum:**
    ```bash
    git clone [https://github.com/vase-github-korisnicko-ime/ime-repozitorijuma.git](https://github.com/vase-github-korisnicko-ime/ime-repozitorijuma.git)
    cd ime-repozitorijuma
    ```
2.  **Otvorite projekat u Eclipse-u:**
    * `File > Import... > Maven > Existing Maven Projects` (ako koristite Maven) ili
    * `File > Import... > General > Existing Projects into Workspace` (ako koristite standardni Java projekat).
3.  **Uverite se da je WindowsBuilder instaliran:**
    * Ako nije, idite na `Help > Install New Software...` i instalirajte `WindowBuilder` sa vašeg Eclipse release update site-a.
4.  **Java Verzija:** Preporučuje se Java 11 ili novija. Ako koristite Java 9+, obavezno dodajte `requires java.desktop;` u svoj `module-info.java` fajl (ako postoji) kako biste omogućili pristup Swing biblioteci.
5.  **Pokrenite aplikaciju:**
    * Desni klik na `src/main/Main.java` > `Run As > Java Application`.


## 📝 Autori

* AT95BL

---