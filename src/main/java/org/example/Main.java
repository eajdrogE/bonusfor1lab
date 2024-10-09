import java.util.*;

public class Main {
    private static final String ENGLISH_FREQUENCY_ORDER = "ETAOINSHRDLCUMWFGYPBVKJXQZ";

    public static void main(String[] args) {
        String cipherText = "EM RMBYWKIYBIO BIIMESI SRYC JRBH EM VMAWMKIMBRWMEC PVFIYFWJIY BEQCWY SWIP WVB RM AWPBVUI BW NRMO IPAEFI NYWU E OIIFCQ VMHEFFQ EMO NYVPBYEBIO ARKRCREM CRNI. HIY NRYPB EBBIUFB EB BELRMS OWJM E PVFIYKRCCERM PIIP HIY URPBELIM NWY WMI BHYVPBRMS HIY RMBW BHI UROPB WN BHI CWAEC AEFI PAIMI’P FWCRBRAP VMJYRBBIM YVCIP EMO EUTRSVWVP UWYECP. EP PHI YRPLP CRNI EMO CRUT BEQCWY NEAIP BHI ORCIUUE WN HEKRMS BW OW BHI JYWMS BHRMSP NWY BHI YRSHB YIEPWMP.";
        String decryptedText = simulatedAnnealingDecrypt(cipherText.toUpperCase());
        System.out.println("Расшифрованный текст: " + decryptedText);
    }

    public static String simulatedAnnealingDecrypt(String cipherText) {
        String bestKey = generateRandomKey();
        String bestDecryption = decryptWithKey(cipherText, bestKey);
        double bestScore = scoreDecryption(bestDecryption);

        double initialScore = scoreDecryption(cipherText);
        System.out.println("Начальный текст: " + cipherText);
        System.out.println("Начальная оценка: " + initialScore);

        double temperature = 1000.0;
        double coolingRate = 0.00001;

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
            case 0: // Обмен двух символов
                int index1 = random.nextInt(key.length());
                int index2 = random.nextInt(key.length());
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
        String[] commonBigrams = {"TH", "HE", "IN",  "AN", "RE", "ND", "AT", "ON", "NT", "HA", "EN", "ES", "ST", "OR", "TE", "OF", "ED", "IS", "IT"};
        String[] commonTrigrams = {"THE", "AND", "ING", "ENT","HER", "FOR", "THA", "NTH", "INT", "TER", "EST", "RES", "HIS", "ERE", "HES", "ALL", "BUT"};
        String[] commonlotrams ={"BEFORE","CHAR", "DEEPLY", "GIRL", "FROM"};
        String[] uncommonBiggrams ={"QZ","XJ", "ZQ", "JQ", "VX"};
        double score = 0;
        for (String bigram : commonBigrams) {
            score += countOccurrences(text, bigram);
        }
        for (String trigram : commonTrigrams) {
            score += countOccurrences(text, trigram)*2;
        }
        for (String trigram : commonlotrams) {
            score += countOccurrences(text, trigram)*10;
        }
        for (String trigram : uncommonBiggrams) {
            score -= countOccurrences(text, trigram)*10;
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
