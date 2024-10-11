import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    private static final String ENGLISH_FREQUENCY_ORDER = "ETAOINSHRDLCUMWFGYPBVKJXQZ";
    private static final double[] ENGLISH_LETTER_FREQUENCIES = {
            12.702, // E
            9.056,  // T
            8.167,  // A
            7.507,  // O
            6.966,  // I
            6.749,  // N
            6.327,  // S
            6.094,  // H
            5.987,  // R
            4.253,  // D
            4.025,  // L
            2.782,  // C
            2.406,  // M
            2.360,  // W
            2.228,  // F
            2.015,  // G
            1.974,  // Y
            1.929,  // P
            1.492,  // B
            0.978,  // V
            0.772,  // K
            0.153,  // J
            0.150,  // X
            0.095,  // Q
            0.074   // Z
    };
    public static void main(String[] args) {
        try {
            String cipherText = new String(Files.readAllBytes(Paths.get("in.txt")));
            String decryptedText = simulatedAnnealingDecrypt(cipherText.toUpperCase());
            System.out.println("Расшифрованный текст: " + decryptedText);
        }
     catch (IOException e) {
        System.out.println("Ошибка при чтении файла: " + e.getMessage());
    }

    }

    public static String simulatedAnnealingDecrypt(String cipherText) {
        String bestKey = generateRandomKey();
        String bestDecryption = decryptWithKey(cipherText, bestKey);
        double bestScore = scoreDecryption(bestDecryption);

        double temperature = 1300.0;
        double coolingRate = 0.00002;

        while (temperature > 1) {
            String newKey = mutateKey(bestKey);
            String newDecryption = decryptWithKey(cipherText, newKey);
            double newScore = scoreDecryption(newDecryption);

            if (acceptanceProbability(bestScore, newScore, temperature) > Math.random()) {
                bestKey = newKey;
                bestDecryption = newDecryption;
                bestScore = newScore;
            }

            temperature *= 1 - coolingRate;
        }

        System.out.println("Лучший ключ: " + bestKey);
        System.out.println("Лучшая оценка: " + bestScore);

        return bestDecryption;
    }

    private static double acceptanceProbability(double bestScore, double newScore, double temperature) {
        if (newScore > bestScore) {
            return 1.0;
        }
        return Math.exp((newScore - bestScore) / temperature);
    }

    private static String generateRandomKey() {
        List<Character> characters = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            characters.add(c);
        }
        Collections.shuffle(characters);
        StringBuilder key = new StringBuilder();
        for (char c : characters) {
            key.append(c);
        }
        return key.toString();
    }

    private static String mutateKey(String key) {
        Random random = new Random();
        int mutationType = random.nextInt(2); // Два типа мутаций

        switch (mutationType) {
            case 0: // Взвешенный обмен двух символов
                int index1 = weightedRandomIndex();
                int index2 = weightedRandomIndex();
                char[] keyChars = key.toCharArray();
                char temp = keyChars[index1];
                keyChars[index1] = keyChars[index2];
                keyChars[index2] = temp;
                return new String(keyChars);

            case 1: // Реверс части ключа
                int start = random.nextInt(key.length());
                int end = random.nextInt(key.length() - start) + start;
                StringBuilder newKey = new StringBuilder(key);
                newKey.replace(start, end, new StringBuilder(key.substring(start, end)).reverse().toString());
                return newKey.toString();

            default:
                return key;
        }
    }

    private static int weightedRandomIndex() {
        double totalWeight = Arrays.stream(ENGLISH_LETTER_FREQUENCIES).sum();
        double random = Math.random() * totalWeight;
        double cumulativeWeight = 0.0;

        for (int i = 0; i < ENGLISH_LETTER_FREQUENCIES.length; i++) {
            cumulativeWeight += ENGLISH_LETTER_FREQUENCIES[i];
            if (random <= cumulativeWeight) {
                return i;
            }
        }
        return ENGLISH_LETTER_FREQUENCIES.length - 1; // На случай, если случайное число на границе
    }

    private static String decryptWithKey(String cipherText, String key) {
        Map<Character, Character> decryptionMap = new HashMap<>();
        for (int i = 0; i < key.length(); i++) {
            decryptionMap.put(key.charAt(i), ENGLISH_FREQUENCY_ORDER.charAt(i));
        }

        StringBuilder decryptedText = new StringBuilder();
        for (char c : cipherText.toCharArray()) {
            if (Character.isLetter(c)) {
                decryptedText.append(decryptionMap.getOrDefault(c, c));
            } else {
                decryptedText.append(c);
            }
        }

        return decryptedText.toString();
    }

    private static double scoreDecryption(String text) {
        String[] commonBigrams = {"TH", "HE", "IN",  "AN", "RE", "ND", "AT", "ON", "NT", "HA", "EN", "ES", "ST", "OR", "TE", "OF", "ED", "IS", "IT", "MY"};
        String[] commonTrigrams = {"THE", "AND", "ING", "ENT","HER", "FOR", "THA", "NTH", "INT", "TER", "EST", "RES", "HIS", "ERE", "HES", "ALL", "BUT","WIT","IRL"};
        String[] uncommonBigrams = {"QZ","XJ", "ZQ", "JQ", "VX" ,"ZZ", "XZ", "QJ", "QX", "ZX", "KQ", "WZ", "VQ", "QW","XV", "KX","JZ","VZ","WX","QY","XY","JY","QK"};
        String[] uncommonBiggrams = {"Z", "X", "Q","J"};
        double score = 0;
        for (String bigram : commonBigrams) {
            score += countOccurrences(text, bigram);
        }
        for (String bigram : uncommonBiggrams) {
            score -= countOccurrences(text, bigram);
        }
        for (String trigram : commonTrigrams) {
            score += countOccurrences(text, trigram) * 2;
        }
        for (String bigram : uncommonBigrams) {
            score -= countOccurrences(text, bigram) * 10;
        }
        return score;
    }

    private static int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}
