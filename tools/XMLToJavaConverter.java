import org.w3c.dom.*;

import java.io.File;
import java.io.FileWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//Documentation: https://zetcode.com/java/dom/

public class XMLToJavaConverter{
    public static void main(String[] args){ //en el arg0 entra el XML y en el arg1 el path de salida del .java
        try{
            //El primer argumento es el archivo XML
            String inputFilePath = args[0];
            File xmlFile = new File(inputFilePath);

            //Cargamos el XML usando DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            //Obtenemos el root como nombre para la clase 
            Element rootElement = document.getDocumentElement();
            String className = rootElement.getNodeName();

            //Creamos el archivo Java
            String javaClassContent = generateJavaClass(className, rootElement);

            //Escribimos el contenido Java a un archivo nuevo
            String outputJavaFile = args[1];
            FileWriter writer = new FileWriter(outputJavaFile);
            writer.write(javaClassContent);
            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    // Método para generar la clase Java a partir del XML
    private static String generateJavaClass(String className, Element rootElement) {
        StringBuilder javaCode = new StringBuilder();

        // Definir la clase Java
        javaCode.append("public class ").append(className).append(" {\n");

        // Iterar sobre los elementos de <inputs> y generar campos para la clase Java
        NodeList inputs = rootElement.getElementsByTagName("inputs");
        if (inputs.getLength() > 0) {
            Element inputElement = (Element) inputs.item(0);
            NodeList inputParams = inputElement.getChildNodes();
            for (int i = 0; i < inputParams.getLength(); i++) {
                Node node = inputParams.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String fieldName = element.getNodeName();
                    javaCode.append("    private String ").append(fieldName).append(";\n");

                    // Generar los métodos getter y setter para la variable
                    javaCode.append("    public String get").append(capitalize(fieldName)).append("() {\n")
                            .append("        return ").append(fieldName).append(";\n")
                            .append("    }\n");

                    javaCode.append("    public void set").append(capitalize(fieldName)).append("(String ").append(fieldName).append(") {\n")
                            .append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n")
                            .append("    }\n");
                }
            }
        }

        // Iterar sobre los elementos de <outputs> y generar campos para la clase Java
        NodeList outputs = rootElement.getElementsByTagName("outputs");
        if (outputs.getLength() > 0) {
            Element outputElement = (Element) outputs.item(0);
            NodeList outputParams = outputElement.getChildNodes();
            for (int i = 0; i < outputParams.getLength(); i++) {
                Node node = outputParams.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String fieldName = element.getNodeName();
                    javaCode.append("    private String ").append(fieldName).append(";\n");

                    // Generar los métodos getter y setter para la variable
                    javaCode.append("    public String get").append(capitalize(fieldName)).append("() {\n")
                            .append("        return ").append(fieldName).append(";\n")
                            .append("    }\n");

                    javaCode.append("    public void set").append(capitalize(fieldName)).append("(String ").append(fieldName).append(") {\n")
                            .append("        this.").append(fieldName).append(" = ").append(fieldName).append(";\n")
                            .append("    }\n");
                }
            }
        }

        // Cerrar la clase
        javaCode.append("}\n");

        return javaCode.toString();
    }

    // Método para capitalizar el nombre de los campos
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}