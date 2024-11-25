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
    public static void main(String[] args) {
        int count = 0;

        String search = "computer";

        try {
            // Criar uma instância de DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Carregar o arquivo XML
            //o document já pode ser acessado
            Document document = builder.parse("verbetesWikipedia.xml");

            // Normalizar o documento
            document.getDocumentElement().normalize();

            // Obter a lista de elementos com a tag "page"
            NodeList nList = document.getElementsByTagName("page");

            List<SimpleEntry<Element, Integer>> list = new ArrayList<>();

            for (int i = 0; i < nList.getLength(); i++) {

                // Obter o nó de cada item
                Node node = nList.item(i);

                //verifica se o nó é um elemento
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    //pega o elemento
                    Element elemento = (Element) node;

                    // extrai e imprimir o conteúdo dos elementos
//                    String id = getElementValueByTagName(elemento, "id");
                    String title = getElementValueByTagName(elemento, "title");
                    String text = getElementValueByTagName(elemento, "text");

                    //"quebra" o title
                    String[] wordsTitle = title.split(" ");

                    //verifica se a busca está no title
                    if (findWord(wordsTitle, search)){
                        String[] textWords = text.split(" ");

                        //conta o número de ocorrencias no text
                        int occurrences = countOccurrencesBySearch(textWords, search);

                        list.add(new SimpleEntry<>(elemento, occurrences));
                    }
                }
            }

            //ordena pelo número de ocorrencias
            list.sort((entry1, entry2) -> - entry1.getValue().compareTo(entry2.getValue()));
            printElements(list, 10);


        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private static void printElements(List<SimpleEntry<Element, Integer>> list, int limit){
        int count = 0;
        for (SimpleEntry<Element, Integer> element : list) {
            String id = getElementValueByTagName(element.getKey(), "id");
            String title = getElementValueByTagName(element.getKey(), "title");
//            String text = getElementValueByTagName(element.getKey(), "text");
            System.out.println(id);
            System.out.println(title);
//            System.out.println(text);
            System.out.println("occurrences: " + element.getValue());
            System.out.println("---------------------");
            count++;
            if (count >= limit){
                break;
            }
        }
    }

}


