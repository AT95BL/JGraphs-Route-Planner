// src/util/ReceiptManager.java
package manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.stream.*;

/**
 * Upravlja cuvanjem racuna i ucitavanjem statistike prodaje.
 */
public class ReceiptManager {

    private static final String RECEIPTS_FOLDER = "receipts";

    /**
     * cuva detalje racuna u tekstualni fajl unutar "receipts" foldera.
     * @param receiptDetails Sadrzaj racuna.
     * @throws IOException Ako dodje do greške pri pisanju fajla.
     */
    public static void saveReceipt(String receiptDetails) throws IOException {
        Path folderPath = Paths.get(RECEIPTS_FOLDER);
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath); // Kreiraj folder ako ne postoji
        }

        String filename = "receipt_" + System.currentTimeMillis() + ".txt"; // Jedinstveno ime fajla
        Path filePath = folderPath.resolve(filename);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(receiptDetails);
        }
    }

    /**
     * Izracunava ukupan broj prodatih karata citajuci sve racune.
     * @return Ukupan broj prodatih karata.
     * @throws IOException Ako dodje do greške pri citanju fajlova.
     */
    public static int getTotalTicketsSold() throws IOException {
        Path folderPath = Paths.get(RECEIPTS_FOLDER);
        if (!Files.exists(folderPath)) {
            return 0; // Nema racuna
        }

        // Broj fajlova u folderu "receipts"
        return (int) Files.list(folderPath).filter(Files::isRegularFile).count();
    }

    /**
     * Izracunava ukupan prihod citajuci sve racune i parsirajuci cene.
     * @return Ukupan prihod.
     * @throws IOException Ako dodje do greške pri citanju fajlova.
     */
    public static double getTotalRevenue() throws IOException {
        Path folderPath = Paths.get(RECEIPTS_FOLDER);
        if (!Files.exists(folderPath)) {
            return 0.0;
        }

        double totalRevenue = 0.0;
        Pattern pricePattern = Pattern.compile("Total cost: (\\d+) E"); // Regex to find ticket price

        try (var stream = Files.list(folderPath)) {
            for (Path filePath : stream.filter(Files::isRegularFile).collect(Collectors.toList())) {
                List<String> lines = Files.readAllLines(filePath);
                for (String line : lines) {
                    Matcher matcher = pricePattern.matcher(line);
                    if (matcher.find()) {
                        try {
                            totalRevenue += Double.parseDouble(matcher.group(1));
                        } catch (NumberFormatException e) {
                            System.err.println("Greška pri parsiranju cene u fajlu " + filePath.getFileName() + ": " + line);
                        }
                        break; // Pretpostavljamo da je cena samo jednom po racunu
                    }
                }
            }
        }
        return totalRevenue;
    }
}