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
 * Upravlja čuvanjem računa i učitavanjem statistike prodaje.
 */
public class ReceiptManager {

    private static final String RECEIPTS_FOLDER = "racuni";

    /**
     * Čuva detalje računa u tekstualni fajl unutar "racuni" foldera.
     * @param receiptDetails Sadržaj računa.
     * @throws IOException Ako dođe do greške pri pisanju fajla.
     */
    public static void saveReceipt(String receiptDetails) throws IOException {
        Path folderPath = Paths.get(RECEIPTS_FOLDER);
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath); // Kreiraj folder ako ne postoji
        }

        String filename = "racun_" + System.currentTimeMillis() + ".txt"; // Jedinstveno ime fajla
        Path filePath = folderPath.resolve(filename);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write(receiptDetails);
        }
    }

    /**
     * Izračunava ukupan broj prodatih karata čitajući sve račune.
     * @return Ukupan broj prodatih karata.
     * @throws IOException Ako dođe do greške pri čitanju fajlova.
     */
    public static int getTotalTicketsSold() throws IOException {
        Path folderPath = Paths.get(RECEIPTS_FOLDER);
        if (!Files.exists(folderPath)) {
            return 0; // Nema računa
        }

        // Broj fajlova u folderu "racuni"
        return (int) Files.list(folderPath).filter(Files::isRegularFile).count();
    }

    /**
     * Izračunava ukupan prihod čitajući sve račune i parsirajući cene.
     * @return Ukupan prihod.
     * @throws IOException Ako dođe do greške pri čitanju fajlova.
     */
    public static double getTotalRevenue() throws IOException {
        Path folderPath = Paths.get(RECEIPTS_FOLDER);
        if (!Files.exists(folderPath)) {
            return 0.0;
        }

        double totalRevenue = 0.0;
        Pattern pricePattern = Pattern.compile("Ukupna cena: (\\d+)€"); // Regex za pronalaženje cene

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
                        break; // Pretpostavljamo da je cena samo jednom po računu
                    }
                }
            }
        }
        return totalRevenue;
    }
}