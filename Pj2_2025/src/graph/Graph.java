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
 * Predstavlja transportni graf sa cvorovima (stanicama) i granama (polascima i transferima).
 */
public class Graph {
    private Map<String, Node> nodes; // Mapa svih cvorova, kljuc je ID cvora (npr. "A_0_0", "Z_0_0")
    private Map<Node, List<Edge>> adjacencies; // Lista susjedstva: za svaki cvor, lista grana koje izlaze iz njega

    public Graph() {
        this.nodes = new HashMap<>();
        this.adjacencies = new HashMap<>();
    }

    /**
     * Dodaje cvor u graf.
     * @param node cvor koji se dodaje.
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
     * Dohvata cvor po njegovom ID-u.
     * @param nodeId ID cvora.
     * @return Pronadjeni cvor ili null ako ne postoji.
     */
    public Node getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * Dohvata listu grana koje izlaze iz datog cvora.
     * @param node Izlazni cvor.
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
     * Kreira cvorove za sve stanice i grane za sve polaske i transfere unutar gradova.
     * @param transportData Objekat koji sadrzi sve transportne podatke.
     */
    public void buildGraph(TransportData transportData) {
        // 1. Kreiranje cvorova (nodes) za sve stanice [cite: 11]
        for (Station s : transportData.getStations()) {
            // Kreiramo cvor za autobusku stanicu
            Node busNode = new Node(s.getBusStation(), s.getCity(), s.getBusStation(), "bus");
            addNode(busNode);

            // Kreiramo cvor za zeljeznicku stanicu
            Node trainNode = new Node(s.getTrainStation(), s.getCity(), s.getTrainStation(), "train");
            addNode(trainNode);
        }

        // 2. Kreiranje grana (edges) za direktne polaske [cite: 6]
        for (Departure d : transportData.getDepartures()) {
            Node sourceNode = getNode(d.getFrom());
            Node destinationNode = getNode(d.getTo()); // Ovo 'to' u Departure objektu je ime grada (G_X_Y), ne stanice!
                                                    // Moramo naci odgovarajucu stanicu u tom gradu

            // Pronadji odgovarajucu stanicu za 'to' destinaciju.
            // Ako je polazak autobusom, destinacija je autobuska stanica u tom gradu.
            // Ako je polazak vozom, destinacija je zeljeznicka stanica u tom gradu.
            if (sourceNode != null && d.getTo() != null) {
                Node actualDestinationNode = null;
                // Logika za odredjivanje dolazne stanice (bus/train)
                String destinationStationId;
                if ("bus".equals(d.getType())) {
                    destinationStationId = "A_" + d.getTo().split("_")[1] + "_" + d.getTo().split("_")[2];
                } else if ("train".equals(d.getType())) {
                    destinationStationId = "Z_" + d.getTo().split("_")[1] + "_" + d.getTo().split("_")[2];
                } else {
                    System.err.println("Unknown transport type: " + d.getType());
                    continue; // Preskoci ovu departure ako je tip nepoznat
                }
                actualDestinationNode = getNode(destinationStationId);


                if (sourceNode != null && actualDestinationNode != null) {
                    addEdge(new Edge(sourceNode, actualDestinationNode, d));
                } else {
                    System.err.println("Error: Node not found for cvor za polazak ili odrediste: " + d.getFrom() + " -> " + d.getTo());
                }
            }
        }

        // 3. Kreiranje grana (edges) za transfere unutar grada [cite: 7]
        for (Station s : transportData.getStations()) {
            Node busNode = getNode(s.getBusStation());
            Node trainNode = getNode(s.getTrainStation());

            if (busNode != null && trainNode != null) {
                // Transfer od autobuske do zeljeznicke stanice
                // Koristimo minTransferTime iz bilo kojeg polaska unutar tog grada,
                // ili jednostavno mozemo uzeti fiksnu vrijednost,
                // ali projektni zadatak specificira da polasci imaju minTransferTime [cite: 6]
                // Uzmimo prosjek ili neku defaultnu vrijednost ako Departure objekat nije povezan direktno sa ovim transferom
                // Za ovu implementaciju, uzecemo fiksnu vrijednost za transfer unutar grada,
                // ili, sto je bolje, dohvatiti minTransferTime iz nekog relevantnog polaska ako je moguce.
                // Jednostavnije je ako su transferi fiksni, ili se racunaju na osnovu prosjeka svih relevantnih minTransferTime.
                // Za sada, stavimo default 20 minuta, ali ovo se moze poboljsati.
                // Prema zadatku, minTransferTime je vezano za polazak, ali logicno je da se primjenjuje i za transfer.
                // Ako pretpostavimo da je minTransferTime generalno za transfer unutar grada:
                // Potrebno je osigurati da su ovi transferi u oba smjera
                int defaultTransferTime = 20; // Primjer, mozete ga definisati u modelu ili izvuci iz podataka ako je smisleno

                // Potrebno je procijeniti kako da se "minTransferTime" primjeni na transfer unutar grada.
                // Ako "minTransferTime" pripada pojedinacnom polasku, onda ovdje imamo problem.
                // Pretpostavimo da je "minTransferTime" iz Departure-a vezano za minimalno vrijeme koje putnik treba da ima
                // da stigne na sljedeci polazak, ne nuzno za fizicki transfer izmedju stanica.
                // Ako projektni zadatak pod "minimalno vrijeme cekanja" misli na transfer izmedju stanica:
                // Za ovu demo implementaciju, koristimo prosjek minTransferTime iz svih Departure objekata za dati grad
                // ili jednostavno fiksnu vrijednost.

                // A_X_Y -> Z_X_Y
                addEdge(new Edge(busNode, trainNode, defaultTransferTime)); // Kreiramo granu za transfer
                // Z_X_Y -> A_X_Y
                addEdge(new Edge(trainNode, busNode, defaultTransferTime)); // I obrnuto
            }
        }
        // Napomena: Detaljnije upravljanje vremenima za transfere unutar grada je kompleksnije.
        // Trenutna implementacija koristi fiksno vrijeme, sto je pojednostavljenje.
        // Idealno, transfer bi zavisio od vremena dolaska prethodnog putovanja i vremena polaska sljedeceg,
        // uzimajuci u obzir `minTransferTime` kao minimalno potrebno vrijeme.
        // Za Dijkstrin algoritam koji trazi najkrace vrijeme, `minTransferTime` se dodaje na `duration`
        // SAMO ako postoji presjedanje, sto znaci da je to vrijeme koje se provede *cekajuci* na sljedeci polazak.
        // Stoga, za grane koje predstavljaju samo transfer izmedju stanica u istom gradu, njihova tezina je samo vrijeme transfera.
    }

    /**
     * Klasa koja predstavlja putanju u grafu, cuvajuci cvorove i grane.
     * Koristi se za pracenje puta tokom Dijkstrinog algoritma.
     */
    public static class Path {
        public List<Node> nodes;
        public List<Edge> edges;
        public double totalWeight; // Ukupna tezina puta
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
            this.arrivalTime = startTime; // Pocetno vrijeme dolaska je vrijeme polaska sa prve stanice
        }

        /**
         * Kreira novu putanju kopirajuci postojecu i dodajuci novu granu i cvor.
         * @param existingPath Postojeca putanja.
         * @param newEdge Nova grana za dodavanje.
         * @param weightToAdd Tezina koju treba dodati ukupnoj tezini.
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
         * Izracunava ukupan broj presjedanja u putanji.
         * Presjedanje se desava kada se tip prevoza promijeni (autobus -> voz ili obrnuto)
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
                    // Provjeri da li je sljedeca grana direktan polazak i da li je tip prevoza drugaciji
                    // ili da li je prethodna grana bila transfer i sada slijedi polazak
                    if (i > 0) {
                        Edge previousEdge = edges.get(i-1);
                        if ("transfer".equals(previousEdge.getType())) {
                            transfers++; // Ako je prethodno bio transfer, pa sad polazak, to je presjedanje
                        } else if (previousEdge.getDepartureDetails() != null &&
                                !previousEdge.getDepartureDetails().getType().equals(currentEdge.getDepartureDetails().getType())) {
                            // Ako je tip prevoza drugaciji, to je presjedanje
                            transfers++;
                        }
                    }
                }
            }
            return transfers;
        }


        /**
         * Izracunava ukupno trajanje putovanja u minutama.
         * @return Ukupno trajanje putovanja u minutama.
         */
        /*
        public long getTotalDurationMinutes() {
            if (edges.isEmpty()) {
                return 0;
            }
            LocalTime startTime = LocalTime.parse(edges.get(0).getDepartureDetails().getDepartureTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime endTime = arrivalTime;

            if (startTime != null && endTime != null) {
                // Racuna razliku u minutama, cak i ako se predje ponoc
                return ChronoUnit.MINUTES.between(startTime, endTime);
            }
            return 0;
        }
        */
        public long getTotalDurationMinutes() {
            if (edges.isEmpty()) return 0;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime startTime = null;

            for (Edge edge : edges) {
                if ("departure".equals(edge.getType()) && edge.getDepartureDetails() != null) {
                    startTime = LocalTime.parse(edge.getDepartureDetails().getDepartureTime(), formatter);
                    break;
                }
            }

            if (startTime != null && arrivalTime != null) {
                return ChronoUnit.MINUTES.between(startTime, arrivalTime);
            }
            return 0;
        }


        /**
         * Izracunava ukupnu cijenu putovanja.
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
     * Implementira Dijkstrin algoritam za pronalazenje optimalne rute izmedju dvije stanice.
     * @param startNodeId ID pocetnog cvora (stanice).
     * @param endNodeId ID odredisnog cvora (stanice).
     * @param criteria Kriterijum optimizacije (vrijeme, cijena, presjedanja).
     * @return Optimalna putanja (Path objekat) ili null ako ruta ne postoji.
     */
    public Path findOptimalRoute(String startNodeId, String endNodeId, OptimizationCriteria criteria) {
        Node startNode = getNode(startNodeId);
        Node endNode = getNode(endNodeId);

        if (startNode == null || endNode == null) {
            System.err.println("Pocetni ili krajnji cvor ne postoji.");
            return null;
        }

        // Mapa za cuvanje najkrace udaljenosti (tezine) do svakog cvora
        Map<Node, Double> distances = new HashMap<>();
        // Mapa za cuvanje prethodne grane na optimalnom putu (za rekonstrukciju puta)
        Map<Node, Edge> predecessors = new HashMap<>();
        // Mapa za cuvanje vremena dolaska na odredjeni cvor
        Map<Node, LocalTime> arrivalTimes = new HashMap<>();
        // PriorityQueue za efikasno dohvacanje cvora sa najmanjom udaljenoscu
        // Redoslijed je baziran na ukupnoj tezini do tog cvora
        PriorityQueue<Path> pq = new PriorityQueue<>(Comparator.comparingDouble(Path::getTotalWeight));


        // Inicijalizacija: sve udaljenosti su beskonacne, osim za pocetni cvor (0)
        for (Node node : nodes.values()) {
            distances.put(node, Double.POSITIVE_INFINITY);
            arrivalTimes.put(node, null);
        }

        // Kreiranje pocetne putanje sa nultom tezinom i polaznim vremenom (za prvu stanicu)
        // Vrijeme polaska sa prve stanice je kljucno za vremenski zavisne rute.
        // Za sada, pretpostavljamo da moze da se krene u bilo koje vreme ako je "od" stanica.
        // Ako je putovanje vremenski zavisno (kao sto je kod autobusa/vozova), trebace nam
        // specificno pocetno vreme polaska. Za sada, neka bude 00:00 ili slicno.
        // U realnom scenariju, GUI bi trebao da omoguci unos vremena polaska.
        // Za demonstraciju, pretpostavimo da je pocetno vrijeme relevantno samo za prvi polazak
        // i da se tada racuna "stvarno" vrijeme putovanja od tog polaska.
        // Za Dijkstru, potrebno je inicijalizovati sa "0" tezinom.
        // Vrijeme ce se koristiti za provjeru validnosti narednih polazaka.

        distances.put(startNode, 0.0);
        // Pocetno vrijeme dolaska na startNode je "sada", ili "vrijeme polaska" koje ce GUI proslijediti
        // Za potrebe Dijkstre, ovo je vrijeme dolaska na taj cvor unutar putanje.
        // Pocinjemo sa "virtualnim" vremenom 0, koje se azurira prvim polaskom.
        arrivalTimes.put(startNode, LocalTime.MIN); // Min Vrijeme je kao "sto prije"

        Path initialPath = new Path(startNode, LocalTime.MIN); // Startujemo sa putanjom od pocetnog cvora
        pq.add(initialPath);

        while (!pq.isEmpty()) {
            Path currentPath = pq.poll();
            Node currentNode = currentPath.getNodes().get(currentPath.getNodes().size() - 1); // Posljednji cvor u putanji
            LocalTime currentArrivalTime = currentPath.getArrivalTime();


            // Ako smo vec pronasli kracu putanju do ovog cvora, preskocimo
            if (currentPath.totalWeight > distances.get(currentNode)) {
                continue;
            }

            // Ako smo stigli do odredisnog cvora, rekonstruisi putanju i vrati je
            if (currentNode.equals(endNode)) {
                return currentPath;
            }

            // Prodji kroz sve susjedne grane
            for (Edge edge : getEdgesFrom(currentNode)) {
                Node neighbor = edge.getDestination();
                double edgeWeight = 0;
                LocalTime nextArrivalTime = null;

                if ("departure".equals(edge.getType())) {
                    Departure d = edge.getDepartureDetails();
                    LocalTime departureTime = LocalTime.parse(d.getDepartureTime(), DateTimeFormatter.ofPattern("HH:mm"));

                    // Provjera validnosti: da li je moguce uhvatiti ovaj polazak?
                    // Vrijeme dolaska na currentNode mora biti PRIJE ili jednako vremenu polaska sljedeceg segmenta.
                    // Takodjer, ako je transfer u pitanju (prethodni edge bio transfer),
                    // onda currentArrivalTime vec ukljucuje transfer vrijeme.
                    // Ako je prethodni segment bio direkni polazak, a sada je novi direkni polazak,
                    // moramo uzeti u obzir minTransferTime za cekanje.

                    long waitingTime = 0; // Vrijeme cekanja na stanici
                    if (currentArrivalTime != LocalTime.MIN) { // Ne cekamo na prvoj stanici
                        if (departureTime.isBefore(currentArrivalTime)) {
                            // Ne mozemo uhvatiti ovaj polazak (vremenski putuje unazad ili je raniji)
                            continue;
                        }
                        waitingTime = ChronoUnit.MINUTES.between(currentArrivalTime, departureTime);

                        // Ako je prethodni segment bio "departure", provjeravamo minTransferTime
                        if (!currentPath.getEdges().isEmpty()) {
                             Edge lastEdgeInPath = currentPath.getEdges().get(currentPath.getEdges().size() - 1);
                             if ("departure".equals(lastEdgeInPath.getType()) && lastEdgeInPath.getDepartureDetails() != null) {
                                 // Ako je cekanje krace od minTransferTime, ne mozemo uhvatiti ovaj polazak
                                 if (waitingTime < lastEdgeInPath.getDepartureDetails().getMinTransferTime()) {
                                     continue;
                                 }
                             }
                        }
                    }

                    // Izracunaj stvarno vrijeme dolaska na odrediste ove grane
                    LocalTime segmentStartTime = departureTime; // Vrijeme kada polazi trenutni segment
                    LocalTime segmentEndTime = segmentStartTime.plusMinutes(d.getDuration()); // Vrijeme dolaska na odrediste ovog segmenta

                    nextArrivalTime = segmentEndTime;

                    // Izracunaj tezinu grane prema kriterijumu
                    switch (criteria) {
                        case TIME:
                            // Ukupno vrijeme = vrijeme trajanja putovanja + vrijeme cekanja
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
                    // Za transfer unutar grada, tezina je samo vrijeme transfera.
                    // Nema provjere vremena polaska jer je to interni transfer.
                    edgeWeight = edge.getWeight(); // Ovo je transferTime

                    nextArrivalTime = currentArrivalTime.plusMinutes((long) edgeWeight);

                    if (criteria == OptimizationCriteria.TRANSFERS) {
                        edgeWeight = 1; // Svaki transfer je 1 "transfer"
                    }
                    // Cijena transfera unutar grada je obicno 0, ali zavisi od specifikacije
                    if (criteria == OptimizationCriteria.PRICE) {
                        edgeWeight = 0; // Pretpostavka: transferi unutar grada su besplatni (ili ukljuceni)
                    }
                }

                // Azuriraj putanju ako smo pronasli kracu
                double newTotalWeight = currentPath.totalWeight + edgeWeight;

                if (newTotalWeight < distances.get(neighbor)) {
                    distances.put(neighbor, newTotalWeight);
                    arrivalTimes.put(neighbor, nextArrivalTime); // Azuriraj vrijeme dolaska za ovaj cvor

                    Path newPath = currentPath.addSegment(edge, edgeWeight, nextArrivalTime);
                    pq.add(newPath);
                }
            }
        }
        return null; // Ruta nije pronadjena
    }
    
    /**
     * Pronalazi vise razlicitih putanja izmedju dva cvora (npr. 2 do 5).
     * Oslanja se na postojecu Dijkstra implementaciju, ali blokira grane iz prethodnih putanja.
     *
     * @param startNodeId ID pocetnog cvora.
     * @param endNodeId ID krajnjeg cvora.
     * @param criteria Kriterijum optimizacije (vrijeme, cijena, presjedanja).
     * @param maxPaths Maksimalan broj putanja koje treba pronaci (npr. 5).
     * @return Lista razlicitih putanja.
     */
    public List<Path> findMultiplePaths(String startNodeId, String endNodeId, OptimizationCriteria criteria, int maxPaths) {
        List<Path> paths = new ArrayList<>();
        Set<String> usedEdges = new HashSet<>();

        for (int attempt = 0; attempt < maxPaths * 3; attempt++) { // pokusavamo vise puta (ne mora uvijek naci put)
            Path path = findOptimalRouteWithEdgeBlock(startNodeId, endNodeId, criteria, usedEdges);
            if (path == null) break;

            paths.add(path);

            // blokiraj barem jednu granu iz pronadjene putanje za sljedeci pokusaj
            for (Edge e : path.getEdges()) {
                String edgeId = e.getSource().getId() + "->" + e.getDestination().getId();
                usedEdges.add(edgeId);
                break; // blokiramo samo jednu granu po putanji
            }

            if (paths.size() >= maxPaths) break;
        }

        return paths;
    }
    
    private Path findOptimalRouteWithEdgeBlock(String startNodeId, String endNodeId, OptimizationCriteria criteria, Set<String> blockedEdges) {
        Node startNode = getNode(startNodeId);
        Node endNode = getNode(endNodeId);
        if (startNode == null || endNode == null) return null;

        Map<Node, Double> distances = new HashMap<>();
        Map<Node, LocalTime> arrivalTimes = new HashMap<>();
        PriorityQueue<Path> pq = new PriorityQueue<>(Comparator.comparingDouble(Path::getTotalWeight));

        for (Node node : nodes.values()) {
            distances.put(node, Double.POSITIVE_INFINITY);
            arrivalTimes.put(node, null);
        }

        distances.put(startNode, 0.0);
        arrivalTimes.put(startNode, LocalTime.MIN);
        pq.add(new Path(startNode, LocalTime.MIN));

        while (!pq.isEmpty()) {
            Path currentPath = pq.poll();
            Node currentNode = currentPath.getNodes().get(currentPath.getNodes().size() - 1);
            LocalTime currentArrivalTime = currentPath.getArrivalTime();

            if (currentPath.totalWeight > distances.get(currentNode)) continue;
            if (currentNode.equals(endNode)) return currentPath;

            for (Edge edge : getEdgesFrom(currentNode)) {
                String edgeId = edge.getSource().getId() + "->" + edge.getDestination().getId();
                if (blockedEdges.contains(edgeId)) continue;

                Node neighbor = edge.getDestination();
                double edgeWeight = 0;
                LocalTime nextArrivalTime = null;

                if ("departure".equals(edge.getType())) {
                    Departure d = edge.getDepartureDetails();
                    LocalTime depTime = LocalTime.parse(d.getDepartureTime(), DateTimeFormatter.ofPattern("HH:mm"));

                    long wait = 0;
                    if (currentArrivalTime != LocalTime.MIN) {
                        if (depTime.isBefore(currentArrivalTime)) continue;
                        wait = ChronoUnit.MINUTES.between(currentArrivalTime, depTime);
                        if (!currentPath.getEdges().isEmpty()) {
                            Edge last = currentPath.getEdges().get(currentPath.getEdges().size() - 1);
                            if ("departure".equals(last.getType()) && last.getDepartureDetails() != null) {
                                if (wait < last.getDepartureDetails().getMinTransferTime()) continue;
                            }
                        }
                    }

                    nextArrivalTime = depTime.plusMinutes(d.getDuration());

                    edgeWeight = switch (criteria) {
                        case TIME -> d.getDuration() + wait;
                        case PRICE -> d.getPrice();
                        case TRANSFERS -> 1;
                    };

                } else if ("transfer".equals(edge.getType())) {
                    edgeWeight = switch (criteria) {
                        case TIME -> edge.getWeight();
                        case PRICE -> 0;
                        case TRANSFERS -> 1;
                    };
                    nextArrivalTime = currentArrivalTime.plusMinutes((long) edge.getWeight());
                }

                double newWeight = currentPath.totalWeight + edgeWeight;

                if (newWeight < distances.get(neighbor)) {
                    distances.put(neighbor, newWeight);
                    arrivalTimes.put(neighbor, nextArrivalTime);
                    Path newPath = currentPath.addSegment(edge, edgeWeight, nextArrivalTime);
                    pq.add(newPath);
                }
            }
        }

        return null;
    }


}