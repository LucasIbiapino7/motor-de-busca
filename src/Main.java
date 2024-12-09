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

        //guarda as informações do verbete (chave - id)
        Map<String, String> cacheInfo = new HashMap<>();

        //Cache
        Map<String, Map<String, Double>> cache = preProcessing(cacheInfo);

        //id e porcentagem
        List<SimpleEntry<String, Double>> result;

        while (true){
            System.out.println("digite o termo de busca:");
            String search = scanner.nextLine().toLowerCase();

            String[] split = search.split(" ");

            if (split.length > 1){
                result = searchTwoWordsInCache(split, cache);
            }else {
                result = searchInCache(search, cache);
            }

            if (result.isEmpty()){
                System.out.println("Nenhum resultado encontrado");
                continue;
            }

            result.stream()
                    .sorted((entry1, entry2) -> - entry1.getValue().compareTo(entry2.getValue()))
                    .limit(5)
                    .map(entry -> cacheInfo.get(entry.getKey()))
                    .forEach(System.out::println);

            System.out.print("digite 0 se quiser parar: ");
            int flag = scanner.nextInt();
            scanner.nextLine();
            if (flag == 0){
                break;
            }
        }
    }

    private static List<SimpleEntry<String, Double>> searchInCache(String search, Map<String, Map<String, Double>> cache) {
        List<SimpleEntry<String, Double>> result = new ArrayList<>();

        for (Map.Entry<String, Map<String, Double>> outerMap : cache.entrySet()){
            //id do verbete
            String id = outerMap.getKey();
            //map com -> palavra como chave e porcentagem como value
            Map<String, Double> map = outerMap.getValue();

            if (map.containsKey(search)){
                SimpleEntry<String, Double> find = new SimpleEntry<>(id, map.get(search));
                result.add(find);
            }
        }
        return result;
    }

    private static List<SimpleEntry<String, Double>> searchTwoWordsInCache(String[] search, Map<String, Map<String, Double>> cache) {
        List<SimpleEntry<String, Double>> result = new ArrayList<>();//Lista pra armazenar o resultado

        //hash que armazena o resultado temporariamente
        Map<String, Double> resultAux = new HashMap<>();

        //percorrendo cada palavra da Busca
        for (String s : search) {
            for (Map.Entry<String, Map<String, Double>> outerEntry : cache.entrySet()) {
                //id do verbete
                String id = outerEntry.getKey();
                //map com -> palavra como chave e porcentagem como value
                Map<String, Double> map = outerEntry.getValue();

                if (map.containsKey(s)) {//map do cache
                    if (resultAux.containsKey(id)){//map com o resultado (id, porcentagem)
                        Double value = resultAux.get(id);
                        value += map.get(s);
                        resultAux.put(id, value);
                    }else {
                        resultAux.put(id, map.get(s));
                    }
                }
            }
        }

        for (Map.Entry<String, Double> map : resultAux.entrySet()){
            String id = map.getKey();
            Double value = map.getValue();
            SimpleEntry<String, Double> find = new SimpleEntry<>(id, value);
            result.add(find);
        }

        return result;
    }

    private static Map<String, Map<String, Double>> preProcessing(Map<String, String> cacheInfo) throws Exception{

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

                //função que processa o id, title e text
                String verbeteInfo = processingVerbete(id, title, text);

                //salva o verbete processado no cache de informações
                cacheInfo.put(id, verbeteInfo);

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

    private static String processingVerbete(String id, String title, String text) {
        int count = 0;
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        sb.append("\n");
        sb.append("title: ");
        sb.append(title);
        sb.append("\n");
        for (String s : text.split(" ")) {
            if (count > 25){
                break;
            }
            sb.append(s);
            sb.append(" ");
            count++;
        }
        sb.append("...");
        return sb.toString();
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
}


