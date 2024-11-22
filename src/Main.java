import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.*;

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

            ArrayList<Element> elementsList = new ArrayList<>();
            TreeMap<Integer, Element> treeMap = new TreeMap<>(Collections.reverseOrder());

            for (int i = 0; i < nList.getLength(); i++) {

                // Obter o nó de cada item
                Node node = nList.item(i);

                //verifica se o nó é um elemento
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    //pega o elemento
                    Element elemento = (Element) node;

                    // extrai e imprimir o conteúdo dos elementos
                    String id = getElementValueByTagName(elemento, "id");
                    String title = getElementValueByTagName(elemento, "title");
                    String text = getElementValueByTagName(elemento, "text");

                    //"quebra" o title
                    String[] words = title.split(" ");

                    if (findWord(words, search)){
                        System.out.println("ID: " + id);
                        System.out.println("Title: " + title);
                        System.out.println("Text: " + text);
                        System.out.println("------");
                    }
                }
            }
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

}


