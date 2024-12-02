import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Predicate;

public class Main {
    private static final int stopWords = 3;
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        Map<String, List<SimpleEntry<Element, Double>>> cache = new HashMap<>();

        while (true){

            System.out.println("Digite a busca: ");
            String search = scanner.next();

            if (cache.containsKey(search)){
                System.out.println("caiu aqui");
                printElements(cache.get(search), 5);

                System.out.println("para sair digite 0 e 1 para continuar: ");
                int flag = scanner.nextInt();
                if (flag == 0){
                    break;
                }

                continue;
            }

            try {
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

                // Lista de "tuplas"
                List<SimpleEntry<Element, Double>> list = new ArrayList<>();

                for (int i = 0; i < nList.getLength(); i++) {

                    // Obter o nó de cada item
                    Node node = nList.item(i);

                    //verifica se o nó é um elemento
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        //pega o elemento
                        Element elemento = (Element) node;

                        // extrai o conteúdo dos elementos
//                    String id = getElementValueByTagName(elemento, "id");
                        String title = getElementValueByTagName(elemento, "title");
                        String text = getElementValueByTagName(elemento, "text");

                        //"quebra" o text
                        String[] textWords = text.split(" ");

                        //verifica se a busca está no text
                        if (findWord(textWords, search)){
                            //conta o número de ocorrencias no text
                            double percentage = countPercentage(textWords, search);

                            //"quebra" o title
                            String[] titleWords = title.split(" ");

                            //Pensar em uma eurística melhor!
                            if (findWord(titleWords, search)){
                                percentage += 0.10;
                            }

                            list.add(new SimpleEntry<>(elemento, percentage));
                        }
                    }
                }

                //Ordena e limita em 5 o resultado
                List<SimpleEntry<Element, Double>> result = list.stream()
                        .sorted((entry1, entry2) -> -entry1.getValue().compareTo(entry2.getValue()))
                        .limit(5)
                        .toList();

                /*
                Salva a busca no cache - chave: palavra da busca (String). Result: Lista de "tuplas (elemento e
                cálculo da porcentagem.
                 */
                cache.put(search, result);

                printElements(result, 5);

                System.out.println("para sair digite 0 e 1 para continuar: ");
                int flag = scanner.nextInt();
                if (flag == 0){
                    break;
                }

                //ordena pelo número de ocorrencias
//                list.sort((entry1, entry2) -> - entry1.getValue().compareTo(entry2.getValue()));
//                printElements(list, 5);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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


