import org.w3c.dom.*;

import java.io.File;
import java.io.FileWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//Documentation: https://zetcode.com/java/dom/

public class XMLToJavaConverter{

    private String[] permitedLabels = {"input", "output"};

    public static void main(String[] args){ //en el arg0 entra el XML y en el arg1 el path de salida del .java y el arg2 donde queremos mover el input XML
        try{
            String inputFilePath = args[0];
            File xmlFile = new File(inputFilePath);

            //Cargamos el XML usando DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            //Creamos el archivo Java
            document.getDocumentElement().normalize();
            String javaClassContent = generateJavaClass(document.getDocumentElement());

            //Escribimos el contenido Java a un archivo nuevo
            String outputJavaFile = args[1];
            FileWriter writer = new FileWriter(outputJavaFile);
            writer.write(javaClassContent);
            writer.close();

            //Movemos el archivo XML a su carpeta correspondiente
            xmlFile.renameTo(new File(args[2]));
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Método para generar la clase Java a partir del XML
    private static String generateJavaClass(Element rootElement) throws InvalidXMLException{

        StringBuilder result = new StringBuilder();

        //CREATE CLASS
        String className = rootElement.getNodeName();
        result.append("public class " + UpperFirstLetter(className) + "{\n\n");

        //GET ATTRIBUTES
        NodeList regionsList = rootElement.getChildNodes();
        NodeList attributesList;
        String attributesResult = "";
        String functionsResult = "";

        //BUCLE DE REGIONES
        for(int i=0; i<regionsList.getLength(); i++){

            Node node = regionsList.item(i);
            System.out.println(node.getNodeName());

            if(isValidNode(node)){

                attributesList = node.getChildNodes();
                //BUCLE DE ATTRIBUTOS
                for(int j=0; j<attributesList.getLength(); j++){

                    node = attributesList.item(j);
                    if(isValidNode(node)){ //POR DEFECTO SERAN STRINGS

                        String nodeName = node.getNodeName();
                        
                        //attribute
                        attributesResult += "\tprivate String " + nodeName + " = \"" + node.getTextContent().replace(" ","") + "\";\n";

                        //getter
                        functionsResult += "\n\tpublic String get" + UpperFirstLetter(nodeName) + "(){\n"
                        + "\t\treturn " + nodeName + ";\n"
                        + "\t}\n";

                        //setter
                        functionsResult += "\n\tpublic void set" + UpperFirstLetter(nodeName) + "(String " + nodeName +"){\n"
                        + "\t\tthis." + nodeName + " = " + nodeName + ";\n"
                        + "\t}\n\n";
                    }
                }
            }
        }
        result.append(attributesResult);
        result.append(functionsResult);
        result.append("}");
        return result.toString();
    }

    private static boolean isValidNode(Node node){
        return Character.isLetter(node.getNodeName().charAt(0));
    }

    private static String UpperFirstLetter(String word){
        if(word.isBlank() || word.isEmpty()) return "";

        String c1 = word.substring(0,1).toUpperCase();
        return c1 + word.substring(1);
    }    

    private static class InvalidXMLException extends Exception{
        public InvalidXMLException(String message){
            super(message);
        }
    }
}

