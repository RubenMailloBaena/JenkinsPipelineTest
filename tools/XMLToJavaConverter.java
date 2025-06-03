import org.w3c.dom.*;

import java.io.File;
import java.io.FileWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//Documentation: https://zetcode.com/java/dom/

public class XMLToJavaConverter{

    //EL STRING DE INPUT DEBE ESTAR SIEMPRE EN PRIMERA POSICION PARA VALIDAR QUE TIENE UNA REGION DE INPUT OBLIGATORIA
    private static String[] permittedLabels = {"input", "output"};
    private static boolean hasInputLabel = false;

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

            if(isValidRegion(node.getNodeName())){

                attributesList = node.getChildNodes();
                //BUCLE DE ATTRIBUTOS
                for(int j=0; j<attributesList.getLength(); j++){

                    node = attributesList.item(j);

                    if(isValidAttribute(node)){ //POR DEFECTO SERAN STRINGS

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

        //COMPROBAMOS QUE TENEMOS EL LABEL DE INPUT NECESARIO
        checkIfHasInputField();

        result.append(attributesResult);
        result.append(functionsResult);
        result.append("}");
        return result.toString();
    }

    private static void checkIfHasInputField() throws InvalidXMLException{
        if(!hasInputLabel)
            throw new InvalidXMLException("[ERROR]: The input .xml does not contain an '" + permittedLabels[0] + "' region/field");
    }

    private static boolean isValidRegion(String nodeName) throws InvalidXMLException{
        if(Character.isLetter(nodeName.charAt(0))){
            if(isPermittedLabel(nodeName))
                return true;
            else
                throw new InvalidXMLException("[ERROR]: Contains an invalid region name --> " + nodeName);
        }
        return false;
    }

    private static boolean isPermittedLabel(String name){
        for(String permitted : permittedLabels){
            if(permitted.equals(name)){ //ESTA DENTRO DE LOS NOMBRES PERMITIDOS
                if(permittedLabels[0].equals(name)) //TENEMOS LA REGION OBLIGATORIA DE INPUTS
                    hasInputLabel = true;
                return true;
            }
        }
        return false;
    }

    private static boolean isValidAttribute(Node node){
        System.out.println(node.getNodeName());
        System.out.println(node.getChildNodes().getLength() == 0);
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

