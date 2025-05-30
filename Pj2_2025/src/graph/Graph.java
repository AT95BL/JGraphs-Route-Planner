// src/graph/Graph.java
package graph;

import model.Departure;
import model.Station;
import model.TransportData;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Predstavlja transportni graf sa čvorovima (stanicama) i granama (polascima i transferima).
 */
public class Graph {
    private Map<String, Node> nodes; // Mapa svih čvorova, ključ je ID čvora (npr. "A_0_0", "Z_0_0")
    private Map<Node, List<Edge>> adjacencies; // Lista susjedstva: za svaki čvor, lista grana koje izlaze iz njega

    public Graph() {
        this.nodes = new HashMap<>();
        this.adjacencies = new HashMap<>();
    }

    /**
     * Dodaje čvor u graf.
     * @param node Čvor koji se dodaje.
     */
    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        adjacencies.putIfAbsent(node, new ArrayList<>());
    }

    /**
     * Dodaje granu u graf.
     * @param edge Grana koja se dodaje.
     */
    public void addEdge(Edge edge) {
        adjacencies.get(edge.getSource()).add(edge);
    }

    /**
     * Dohvata čvor po njegovom ID-u.
     * @param nodeId ID čvora.
     * @return Pronađeni čvor ili null ako ne postoji.
     */
    public Node getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * Dohvata listu grana koje izlaze iz datog čvora.
     * @param node Izlazni čvor.
     * @return Lista grana.
     */
    public List<Edge> getEdgesFrom(Node node) {
        return adjacencies.getOrDefault(node, Collections.emptyList());
    }

    public Collection<Node> getAllNodes() {
        return nodes.values();
    }

    public Map<String, Node> getNodesMap() {
        return nodes;
    }

    /**
     * Gradi graf na osnovu TransportData objekta.
     * Kreira čvorove za sve stanice i grane za sve polaske i transfere unutar gradova.
     * @param transportData Objekat koji sadrži sve transportne podatke.
     */
    public void buildGraph(TransportData transportData) {
        // 1. Kreiranje čvorova (nodes) za sve stanice [cite: 11]
        for (Station s : transportData.getStations()) {
            // Kreiramo čvor za autobusku stanicu
            Node busNode = new Node(s.getBusStation(), s.getCity(), s.getBusStation(), "bus");
            addNode(busNode);

            // Kreiramo čvor za željezničku stanicu
            Node trainNode = new Node(s.getTrainStation(), s.getCity(), s.getTrainStation(), "train");
            addNode(trainNode);
        }

        // 2. Kreiranje grana (edges) za direktne polaske [cite: 6]
        for (Departure d : transportData.getDepartures()) {
            Node sourceNode = getNode(d.getFrom());
            Node destinationNode = getNode(d.getTo()); // Ovo 'to' u Departure objektu je ime grada (G_X_Y), ne stanice!
                                                    // Moramo naći odgovarajuću stanicu u tom gradu

            // Pronađi odgovarajuću stanicu za 'to' destinaciju.
            // Ako je polazak autobusom, destinacija je autobuska stanica u tom gradu.
            // Ako je polazak vozom, destinacija je željeznička stanica u tom gradu.
            if (sourceNode != null && d.getTo() != null) {
                Node actualDestinationNode = null;
                // Logika za određivanje dolazne stanice (bus/train)
                String destinationStationId;
                if ("autobus".equals(d.getType())) {
                    destinationStationId = "A_" + d.getTo().split("_")[1] + "_" + d.getTo().split("_")[2];
                } else if ("voz".equals(d.getType())) {
                    destinationStationId = "Z_" + d.getTo().split("_")[1] + "_" + d.getTo().split("_")[2];
                } else {
                    System.err.println("Nepoznat tip prevoza: " + d.getType());
                    continue; // Preskoči ovu departure ako je tip nepoznat
                }
                actualDestinationNode = getNode(destinationStationId);


                if (sourceNode != null && actualDestinationNode != null) {
                    addEdge(new Edge(sourceNode, actualDestinationNode, d));
                } else {
                    System.err.println("Greška: Nije pronađen čvor za polazak ili odredište: " + d.getFrom() + " -> " + d.getTo());
                }
            }
        }

        // 3. Kreiranje grana (edges) za transfere unutar grada [cite: 7]
        for (Station s : transportData.getStations()) {
            Node busNode = getNode(s.getBusStation());
            Node trainNode = getNode(s.getTrainStation());

            if (busNode != null && trainNode != null) {
                // Transfer od autobuske do željezničke stanice
                // Koristimo minTransferTime iz bilo kojeg polaska unutar tog grada,
                // ili jednostavno možemo uzeti fiksnu vrijednost,
                // ali projektni zadatak specificira da polasci imaju minTransferTime [cite: 6]
                // Uzmimo prosjek ili neku defaultnu vrijednost ako Departure objekat nije povezan direktno sa ovim transferom
                // Za ovu implementaciju, uzećemo fiksnu vrijednost za transfer unutar grada,
                // ili, što je bolje, dohvatiti minTransferTime iz nekog relevantnog polaska ako je moguće.
                // Jednostavnije je ako su transferi fiksni, ili se racunaju na osnovu prosjeka svih relevantnih minTransferTime.
                // Za sada, stavimo default 20 minuta, ali ovo se može poboljšati.
                // Prema zadatku, minTransferTime je vezano za polazak, ali logično je da se primjenjuje i za transfer.
                // Ako pretpostavimo da je minTransferTime generalno za transfer unutar grada:
                // Potrebno je osigurati da su ovi transferi u oba smjera
                int defaultTransferTime = 20; // Primjer, možete ga definisati u modelu ili izvući iz podataka ako je smisleno

                // Potrebno je procijeniti kako da se "minTransferTime" primjeni na transfer unutar grada.
                // Ako "minTransferTime" pripada pojedinačnom polasku, onda ovdje imamo problem.
                // Pretpostavimo da je "minTransferTime" iz Departure-a vezano za minimalno vrijeme koje putnik treba da ima
                // da stigne na sljedeći polazak, ne nužno za fizički transfer između stanica.
                // Ako projektni zadatak pod "minimalno vrijeme čekanja" misli na transfer između stanica:
                // Za ovu demo implementaciju, koristimo prosjek minTransferTime iz svih Departure objekata za dati grad
                // ili jednostavno fiksnu vrijednost.

                // A_X_Y -> Z_X_Y
                addEdge(new Edge(busNode, trainNode, defaultTransferTime)); // Kreiramo granu za transfer
                // Z_X_Y -> A_X_Y
                addEdge(new Edge(trainNode, busNode, defaultTransferTime)); // I obrnuto
            }
        }
        // Napomena: Detaljnije upravljanje vremenima za transfere unutar grada je kompleksnije.
        // Trenutna implementacija koristi fiksno vrijeme, što je pojednostavljenje.
        // Idealno, transfer bi zavisio od vremena dolaska prethodnog putovanja i vremena polaska sljedećeg,
        // uzimajući u obzir `minTransferTime` kao minimalno potrebno vrijeme.
        // Za Dijkstrin algoritam koji traži najkraće vrijeme, `minTransferTime` se dodaje na `duration`
        // SAMO ako postoji presjedanje, što znači da je to vrijeme koje se provede *čekajući* na sljedeći polazak.
        // Stoga, za grane koje predstavljaju samo transfer između stanica u istom gradu, njihova težina je samo vrijeme transfera.
    }

    /**
     * Klasa koja predstavlja putanju u grafu, čuvajući čvorove i grane.
     * Koristi se za praćenje puta tokom Dijkstrinog algoritma.
     */
    public static class Path {
        public List<Node> nodes;
        public List<Edge> edges;
        public double totalWeight; // Ukupna težina puta
        public LocalTime arrivalTime; // Vrijeme dolaska na krajnju destinaciju

        public Path() {
            this.nodes = new ArrayList<>();
            this.edges = new ArrayList<>();
            this.totalWeight = 0;
            this.arrivalTime = null;
        }

        public Path(Node startNode, LocalTime startTime) {
            this();
            this.nodes.add(startNode);
            this.arrivalTime = startTime; // Početno vrijeme dolaska je vrijeme polaska sa prve stanice
        }

        /**
         * Kreira novu putanju kopirajući postojeću i dodajući novu granu i čvor.
         * @param existingPath Postojeća putanja.
         * @param newEdge Nova grana za dodavanje.
         * @param weightToAdd Težina koju treba dodati ukupnoj težini.
         * @param newArrivalTime Vrijeme dolaska na kraju nove grane.
         * @return Nova putanja sa dodatim elementima.
         */
        public Path addSegment(Edge newEdge, double weightToAdd, LocalTime newArrivalTime) {
            Path newPath = new Path();
            newPath.nodes.addAll(this.nodes);
            newPath.edges.addAll(this.edges);
            newPath.totalWeight = this.totalWeight + weightToAdd;
            newPath.arrivalTime = newArrivalTime;

            newPath.nodes.add(newEdge.getDestination());
            newPath.edges.add(newEdge);

            return newPath;
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public List<Edge> getEdges() {
            return edges;
        }

        public double getTotalWeight() {
            return totalWeight;
        }

        public LocalTime getArrivalTime() {
            return arrivalTime;
        }

        /**
         * Izračunava ukupan broj presjedanja u putanji.
         * Presjedanje se dešava kada se tip prevoza promijeni (autobus -> voz ili obrnuto)
         * ili kada se promijeni stanica unutar istog grada (transfer).
         * @return Broj presjedanja.
         */
        public int getNumberOfTransfers() {
            int transfers = 0;
            if (edges.isEmpty()) {
                return 0;
            }

            // Prvi segment ne broji presjedanje
            for (int i = 0; i < edges.size(); i++) {
                Edge currentEdge = edges.get(i);
                if ("transfer".equals(currentEdge.getType())) {
                    transfers++; // Svaki eksplicitni transfer unutar grada je presjedanje
                } else if (currentEdge.getDepartureDetails() != null) {
                    // Provjeri da li je sljedeća grana direktan polazak i da li je tip prevoza drugačiji
                    // ili da li je prethodna grana bila transfer i sada slijedi polazak
                    if (i > 0) {
                        Edge previousEdge = edges.get(i-1);
                        if ("transfer".equals(previousEdge.getType())) {
                            transfers++; // Ako je prethodno bio transfer, pa sad polazak, to je presjedanje
                        } else if (previousEdge.getDepartureDetails() != null &&
                                !previousEdge.getDepartureDetails().getType().equals(currentEdge.getDepartureDetails().getType())) {
                            // Ako je tip prevoza drugačiji, to je presjedanje
                            transfers++;
                        }
                    }
                }
            }
            return transfers;
        }


        /**
         * Izračunava ukupno trajanje putovanja u minutama.
         * @return Ukupno trajanje putovanja u minutama.
         */
        public long getTotalDurationMinutes() {
            if (edges.isEmpty()) {
                return 0;
            }
            LocalTime startTime = LocalTime.parse(edges.get(0).getDepartureDetails().getDepartureTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime endTime = arrivalTime;

            if (startTime != null && endTime != null) {
                // Računa razliku u minutama, čak i ako se pređe ponoć
                return ChronoUnit.MINUTES.between(startTime, endTime);
            }
            return 0;
        }

        /**
         * Izračunava ukupnu cijenu putovanja.
         * @return Ukupna cijena putovanja.
         */
        public int getTotalPrice() {
            int total = 0;
            for (Edge edge : edges) {
                if ("departure".equals(edge.getType()) && edge.getDepartureDetails() != null) {
                    total += edge.getDepartureDetails().getPrice();
                }
                // Transferi nemaju cijenu karte, samo vrijeme
            }
            return total;
        }
    }
    
 // ... (u klasi graph.Graph.java)

    /**
     * Enumeracija za kriterijume optimizacije.
     */
    public enum OptimizationCriteria {
        TIME,
        PRICE,
        TRANSFERS
    }

    /**
     * Implementira Dijkstrin algoritam za pronalaženje optimalne rute između dvije stanice.
     * @param startNodeId ID početnog čvora (stanice).
     * @param endNodeId ID odredišnog čvora (stanice).
     * @param criteria Kriterijum optimizacije (vrijeme, cijena, presjedanja).
     * @return Optimalna putanja (Path objekat) ili null ako ruta ne postoji.
     */
    public Path findOptimalRoute(String startNodeId, String endNodeId, OptimizationCriteria criteria) {
        Node startNode = getNode(startNodeId);
        Node endNode = getNode(endNodeId);

        if (startNode == null || endNode == null) {
            System.err.println("Početni ili krajnji čvor ne postoji.");
            return null;
        }

        // Mapa za čuvanje najkraće udaljenosti (težine) do svakog čvora
        Map<Node, Double> distances = new HashMap<>();
        // Mapa za čuvanje prethodne grane na optimalnom putu (za rekonstrukciju puta)
        Map<Node, Edge> predecessors = new HashMap<>();
        // Mapa za čuvanje vremena dolaska na određeni čvor
        Map<Node, LocalTime> arrivalTimes = new HashMap<>();
        // PriorityQueue za efikasno dohvaćanje čvora sa najmanjom udaljenošću
        // Redoslijed je baziran na ukupnoj težini do tog čvora
        PriorityQueue<Path> pq = new PriorityQueue<>(Comparator.comparingDouble(Path::getTotalWeight));


        // Inicijalizacija: sve udaljenosti su beskonačne, osim za početni čvor (0)
        for (Node node : nodes.values()) {
            distances.put(node, Double.POSITIVE_INFINITY);
            arrivalTimes.put(node, null);
        }

        // Kreiranje početne putanje sa nultom težinom i polaznim vremenom (za prvu stanicu)
        // Vrijeme polaska sa prve stanice je ključno za vremenski zavisne rute.
        // Za sada, pretpostavljamo da može da se krene u bilo koje vreme ako je "od" stanica.
        // Ako je putovanje vremenski zavisno (kao što je kod autobusa/vozova), trebaće nam
        // specificno početno vreme polaska. Za sada, neka bude 00:00 ili slično.
        // U realnom scenariju, GUI bi trebao da omogući unos vremena polaska.
        // Za demonstraciju, pretpostavimo da je početno vrijeme relevantno samo za prvi polazak
        // i da se tada računa "stvarno" vrijeme putovanja od tog polaska.
        // Za Dijkstru, potrebno je inicijalizovati sa "0" težinom.
        // Vrijeme će se koristiti za provjeru validnosti narednih polazaka.

        distances.put(startNode, 0.0);
        // Početno vrijeme dolaska na startNode je "sada", ili "vrijeme polaska" koje će GUI proslijediti
        // Za potrebe Dijkstre, ovo je vrijeme dolaska na taj čvor unutar putanje.
        // Počinjemo sa "virtualnim" vremenom 0, koje se ažurira prvim polaskom.
        arrivalTimes.put(startNode, LocalTime.MIN); // Min Vrijeme je kao "što prije"

        Path initialPath = new Path(startNode, LocalTime.MIN); // Startujemo sa putanjom od početnog čvora
        pq.add(initialPath);

        while (!pq.isEmpty()) {
            Path currentPath = pq.poll();
            Node currentNode = currentPath.getNodes().get(currentPath.getNodes().size() - 1); // Posljednji čvor u putanji
            LocalTime currentArrivalTime = currentPath.getArrivalTime();


            // Ako smo već pronašli kraću putanju do ovog čvora, preskočimo
            if (currentPath.totalWeight > distances.get(currentNode)) {
                continue;
            }

            // Ako smo stigli do odredišnog čvora, rekonstruiši putanju i vrati je
            if (currentNode.equals(endNode)) {
                return currentPath;
            }

            // Prođi kroz sve susjedne grane
            for (Edge edge : getEdgesFrom(currentNode)) {
                Node neighbor = edge.getDestination();
                double edgeWeight = 0;
                LocalTime nextArrivalTime = null;

                if ("departure".equals(edge.getType())) {
                    Departure d = edge.getDepartureDetails();
                    LocalTime departureTime = LocalTime.parse(d.getDepartureTime(), DateTimeFormatter.ofPattern("HH:mm"));

                    // Provjera validnosti: da li je moguće uhvatiti ovaj polazak?
                    // Vrijeme dolaska na currentNode mora biti PRIJE ili jednako vremenu polaska sljedećeg segmenta.
                    // Također, ako je transfer u pitanju (prethodni edge bio transfer),
                    // onda currentArrivalTime već uključuje transfer vrijeme.
                    // Ako je prethodni segment bio direkni polazak, a sada je novi direkni polazak,
                    // moramo uzeti u obzir minTransferTime za čekanje.

                    long waitingTime = 0; // Vrijeme čekanja na stanici
                    if (currentArrivalTime != LocalTime.MIN) { // Ne čekamo na prvoj stanici
                        if (departureTime.isBefore(currentArrivalTime)) {
                            // Ne možemo uhvatiti ovaj polazak (vremenski putuje unazad ili je raniji)
                            continue;
                        }
                        waitingTime = ChronoUnit.MINUTES.between(currentArrivalTime, departureTime);

                        // Ako je prethodni segment bio "departure", provjeravamo minTransferTime
                        if (!currentPath.getEdges().isEmpty()) {
                             Edge lastEdgeInPath = currentPath.getEdges().get(currentPath.getEdges().size() - 1);
                             if ("departure".equals(lastEdgeInPath.getType()) && lastEdgeInPath.getDepartureDetails() != null) {
                                 // Ako je čekanje kraće od minTransferTime, ne možemo uhvatiti ovaj polazak
                                 if (waitingTime < lastEdgeInPath.getDepartureDetails().getMinTransferTime()) {
                                     continue;
                                 }
                             }
                        }
                    }

                    // Izračunaj stvarno vrijeme dolaska na odredište ove grane
                    LocalTime segmentStartTime = departureTime; // Vrijeme kada polazi trenutni segment
                    LocalTime segmentEndTime = segmentStartTime.plusMinutes(d.getDuration()); // Vrijeme dolaska na odredište ovog segmenta

                    nextArrivalTime = segmentEndTime;

                    // Izračunaj težinu grane prema kriterijumu
                    switch (criteria) {
                        case TIME:
                            // Ukupno vrijeme = vrijeme trajanja putovanja + vrijeme čekanja
                            edgeWeight = d.getDuration() + waitingTime; // Ukupno vrijeme provedeno u ovom segmentu
                            break;
                        case PRICE:
                            edgeWeight = d.getPrice();
                            break;
                        case TRANSFERS:
                            edgeWeight = 1; // Svaki direktan polazak je 1 "transfer" u ovom kontekstu
                            break;
                    }

                } else if ("transfer".equals(edge.getType())) {
                    // Za transfer unutar grada, težina je samo vrijeme transfera.
                    // Nema provjere vremena polaska jer je to interni transfer.
                    edgeWeight = edge.getWeight(); // Ovo je transferTime

                    nextArrivalTime = currentArrivalTime.plusMinutes((long) edgeWeight);

                    if (criteria == OptimizationCriteria.TRANSFERS) {
                        edgeWeight = 1; // Svaki transfer je 1 "transfer"
                    }
                    // Cijena transfera unutar grada je obično 0, ali zavisi od specifikacije
                    if (criteria == OptimizationCriteria.PRICE) {
                        edgeWeight = 0; // Pretpostavka: transferi unutar grada su besplatni (ili uključeni)
                    }
                }

                // Ažuriraj putanju ako smo pronašli kraću
                double newTotalWeight = currentPath.totalWeight + edgeWeight;

                if (newTotalWeight < distances.get(neighbor)) {
                    distances.put(neighbor, newTotalWeight);
                    arrivalTimes.put(neighbor, nextArrivalTime); // Ažuriraj vrijeme dolaska za ovaj čvor

                    Path newPath = currentPath.addSegment(edge, edgeWeight, nextArrivalTime);
                    pq.add(newPath);
                }
            }
        }
        return null; // Ruta nije pronađena
    }
}






