import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class Main {
    private static final int stopWords = 3;
    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        //Cache
        Map<String, Map<String, Double>> cache = preProcessing();

        /*
        como funciona a busca?
        quando recebemos um verbete, percorremos o cache, em cada id perguntamos se aquele verbete
        tem a palavra que buscamos, se sim, retornamos a sua porcentagem, depois ordenamos por porcentagem e exibimos
        os valores
         */

        //id e porcentagem
        List<SimpleEntry<String, Double>> result;

        while (true){
            System.out.println("digite o termo de busca:");
            String search = scanner.next().toLowerCase();

            result = searchInCache(search, cache);

            result.stream()
                    .sorted((entry1, entry2) -> - entry1.getValue().compareTo(entry2.getValue()))
                    .limit(5)
                    .forEach(System.out::println);

            System.out.println("digite 0 se quiser parar");
            int flag = scanner.nextInt();
            if (flag == 0){
                break;
            }
        }

    }

    private static List<SimpleEntry<String, Double>> searchInCache(String search, Map<String, Map<String, Double>> cache) {
        List<SimpleEntry<String, Double>> result = new ArrayList<>();

        for (Map.Entry<String, Map<String, Double>> outerEntry : cache.entrySet()){
            //id do verbete
            String id = outerEntry.getKey();
            //map com -> palavra como chave e porcentagem como value
            Map<String, Double> map = outerEntry.getValue();

            if (map.containsKey(search)){
                SimpleEntry<String, Double> find = new SimpleEntry<>(id, map.get(search));
                result.add(find);
            }
        }

        return result;
    }

    private static Map<String, Map<String, Double>> preProcessing() throws Exception{

        Map<String, Map<String, Double>> cache = new HashMap<>();

        // Cria uma instância de DocumentBuilderFactory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Carrega o arquivo XML
        //o document já pode ser acessado
        Document document = builder.parse("verbetesWikipedia.xml");

        // Normalizar o documento
        document.getDocumentElement().normalize();

        // Obter a lista de elementos com a tag "page"
        NodeList nList = document.getElementsByTagName("page");

        for (int i = 0; i < nList.getLength(); i++) {
            // Obter o nó de cada item
            Node node = nList.item(i);

            //verifica se o nó é um elemento
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                //pega o elemento
                Element elemento = (Element) node;

                //Hash que guarda as ocorrencias e a porcentagem
                //chave - Palavra, valor - porcentagem
                Map<String, Double> occurrences = new HashMap<>();

                // extrai o conteúdo dos elementos
                String id = getElementValueByTagName(elemento, "id");
                String title = getElementValueByTagName(elemento, "title");
                String text = getElementValueByTagName(elemento, "text");

                //"quebra" o text e o title
                String[] textWords = text.split(" ");
                String[] titleWords = title.split(" ");

                int textSize = 0;

                //percorre o texto
                for (String word : textWords) {
                    //conta o numero de palavras do texto
                    if (word.length() > stopWords){
                        textSize++;

                        //insere uma nova palavra no hash
                        if (!occurrences.containsKey(word.toLowerCase())){
                            occurrences.put(word.toLowerCase(), 1.0);
                        }else {//incrementa o valor de uma palavra
                            Double value = occurrences.get(word.toLowerCase());
                            value++;
                            occurrences.put(word.toLowerCase(), value);
                        }
                    }
                }

                //atualiza o valor do occurrences para porcentagem
                for (Map.Entry<String, Double> entry : occurrences.entrySet()) {
                    String key = entry.getKey();
                    Double value = (double) entry.getValue() / textSize;
                    occurrences.put(key, value);
                }

                for (String word : titleWords) {
                    if (occurrences.containsKey(word.toLowerCase())){
                        Double value = occurrences.get(word.toLowerCase());
                        value += 0.10;
                        occurrences.put(word.toLowerCase(), value);
                    }
                }
                //adiciona o map de occurrences no cache principal
                cache.put(id, occurrences);
            }
        }

        return cache;
    }

    //conta a porcentagem de aparição da busca em relação ao texto
    // calculo: ocorrencias / palavras do texto que não são stop words
    private static double countPercentage(String[] textWords, String search) {
        //Número de ocorrencias da palavra no texto
        int occurrences = countOccurrencesBySearch(textWords, search);

        //número de palavras no texto sem as stopWords
        int occurrencesLessStopWords = countLessStopWords(textWords);

        return (double) occurrences / occurrencesLessStopWords;
    }

    //Conta o número de palavras do texto sem levar em consideração as stopwords
    private static int countLessStopWords(String[] textWords) {
        int count = 0;
        for (String textWord : textWords) {
            if (textWord.length() > stopWords){
                count++;
            }
        }
        return count;
    }

    // Método para extrair o valor de um elemento com base no nome da tag
    private static String getElementValueByTagName(Element element, String tagName) {
        // Novamente, verifica um nó com esse nome, só que agora dentro do "page"
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent().trim();
        }
        return "";
    }

    private static boolean findWord(String[] words, String search){
        for (String word : words) {
            if (word.equalsIgnoreCase(search)){
                return true;
            }
        }
        return false;
    }

    private static int countOccurrencesBySearch(String[] words, String search){
        int count = 0;
        for (String word : words) {
            if (word.equalsIgnoreCase(search)){
                count++;
            }
        }
        return count;
    }


    private static void printElements(List<SimpleEntry<Element, Double>> list, int limit){
        int count = 0;
        for (SimpleEntry<Element, Double> element : list) {
            String id = getElementValueByTagName(element.getKey(), "id");
            String title = getElementValueByTagName(element.getKey(), "title");
//            String text = getElementValueByTagName(element.getKey(), "text");
            System.out.println(id);
            System.out.println(title);
//            System.out.println(text);
            System.out.print("percentage: ");
            System.out.printf("%.8f\n", element.getValue());
            System.out.println("---------------------");
            count++;
            if (count >= limit){
                break;
            }
        }
    }
}


