import org.w3c.dom.*;

import java.io.File;
import java.io.FileWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//Documentation: https://zetcode.com/java/dom/

public class XMLToJavaConverter{

    //EL STRING DE INPUT DEBE ESTAR SIEMPRE EN PRIMERA POSICION PARA VALIDAR QUE TIENE UNA REGION DE INPUT OBLIGATORIA
    private static String[] permittedLabels = {"input", "output"};

    //pos 1. Nombre del atributo que indica el tipo de varaible
    //pos 2. ---
    private static String[] permittedAttributes = {"varType"};

    //Escribir en Lower Case
    private static String[] permitedVarTypes = {"string", "boolean", "integer", "float", "double"};

    private static boolean hasInputRegion = false;
    private static boolean lastTypeWasString = false;

    private static final int MAX_VARIABLE_ATTRIBUTES = 1;
    private static final int MAX_VARIABLE_CHILDS = 1;

    //en el arg0 entra el XML y en el arg1 el path de salida del .java y el arg2 donde queremos mover el input XML
    public static void main(String[] args){ 
        
        if (args.length < 3) {
            System.err.println("Usage: XMLToJavaConverter <input XML path> <output Java path> <move XML path>");
            System.exit(1);
        }

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

        NodeList regionsList = rootElement.getChildNodes();
        NodeList variablesList;
        String variablesResult = "";
        String functionsResult = "";

        //BUCLE DE REGIONES (INPUT / OUTPUT)
        for(int i=0; i<regionsList.getLength(); i++){

            Node node = regionsList.item(i);

            if(isValidRegion(node.getNodeName())){

                variablesList = node.getChildNodes();

                //BUCLE DE VARIABLES (PARAM1 / RESULT2)
                for(int j=0; j<variablesList.getLength(); j++){

                    node = variablesList.item(j);

                    if(isValidVariable(node)){ 

                        //VALIDAMOS QUE EL EL ATRIBUTO Y LA VARAIBLE SON CORRECTOS; Y MAPEAMOS EL NOMBRE
                        String varType = mapVariableTypes(isValidVariableType(node));
                        String nodeName = node.getNodeName();
                        
                        //attribute (ej. private String param1)
                        variablesResult += "\tprivate " + varType + " " + nodeName;

                        //attribute value (ej. = "Valor1";) Si es String añadimos comillas.
                        if(lastTypeWasString)
                            variablesResult += " = \"" + node.getTextContent().replace(" ","") + "\";\n";
                        else
                            variablesResult += " = " + node.getTextContent().replace(" ","") + ";\n";

                        //getter 
                        functionsResult += "\n\tpublic " + varType + " get" + UpperFirstLetter(nodeName) + "(){\n"
                        + "\t\treturn " + nodeName + ";\n"
                        + "\t}\n";

                        //setter
                        functionsResult += "\n\tpublic void set" + UpperFirstLetter(nodeName) + "(" + varType + " " + nodeName +"){\n"
                        + "\t\tthis." + nodeName + " = " + nodeName + ";\n"
                        + "\t}\n\n";
                    }
                }
            }
        }

        //COMPROBAMOS QUE TENEMOS EL LABEL DE INPUT NECESARIO
        checkIfHasInputField();

        result.append(variablesResult);
        result.append(functionsResult);
        result.append("}");
        return result.toString();
    }

    //VALIDAMOS QUE TENEMOS LA REGION INPUT
    private static void checkIfHasInputField() throws InvalidXMLException{
        if(!hasInputRegion)
            throw new InvalidXMLException("[ERROR]: The input .xml does not contain an '" + permittedLabels[0] + "' region/field");
    }

    //VALIDAMOS QUE LA REGION ESTE BIEN ESCRITA Y SI ES EL INPUT, QUE ES OBLIGATORIO
    private static boolean isValidRegion(String nodeName) throws InvalidXMLException{
        if(Character.isLetter(nodeName.charAt(0))){
            if(!isPermitted(nodeName, permittedLabels, true, true)) //ESTA DENTRO DE LOS NOMBRES PERMITIDOS
                throw new InvalidXMLException("[ERROR]: Contains an invalid region name --> " + nodeName);
            return true;
        }
        return false;
    }
    
    //VALIDAMOS QUE LA VARAIBLE Y ATRIBUTO DEL XML SEA CORRECTO
    private static boolean isValidVariable(Node node) throws InvalidXMLException{
        if(Character.isLetter(node.getNodeName().charAt(0))){

            if(node.getChildNodes().getLength() != MAX_VARIABLE_CHILDS) //VALIDAMOS QUE LAS VARIABLES SOLO TENGAN UN SOLO HIJO
                throw new InvalidXMLException("[ERROR]: Variables can't have more than " + MAX_VARIABLE_CHILDS +" childs or be empty! --> " + node.getNodeName());

            if(node.getAttributes().getLength() != MAX_VARIABLE_ATTRIBUTES) //VALIDA QUE SOLO TENGAMOS UN ATTRIBUTO
                throw new InvalidXMLException("[ERROR]: Variables can't have more than " + MAX_VARIABLE_ATTRIBUTES + " attributes or be empty! --> " + node.getNodeName());

            return true;
        }
        return false;
    }

    //VALIDAMOS QUE ES UN TIPO DE VARIABLE QUE EXISTE
    private static String isValidVariableType(Node node) throws InvalidXMLException{
        
        String attributeName = node.getAttributes().item(0).getNodeName();

        if(!isPermitted(attributeName, permittedAttributes, true, false)) //ATRIBUTO QUE EXISTE
            throw new InvalidXMLException("[ERROR]: Invalid attribute name: --> " + attributeName);

        String varType = node.getAttributes().getNamedItem(attributeName).getTextContent();

        if(!isPermitted(varType, permitedVarTypes, true, false)) //TIPO DE VARIABLE QUE EXISTE
            throw new InvalidXMLException("[ERROR]: Invalid variable type: --> " + varType);
                
        return varType.toLowerCase();
    }

    //VALIDAMOS SI LOS NOMBRES ESTAN DENTRO DE LA COLECCION INDICADA, SI VENIMOS DE LAS REGIONES; PODEMOS VALIDAR SI ESTA EL INPUT OBLIGATORIO
    //Y TENEMOS OTRO BOOL PARA INDICAR SI DEBEMOS ESCRIBIR TODO IGUAL, O SI PODEMOS IGNORAR LAS MAYUSCULAS.
    private static boolean isPermitted(String inputName, String[] permittedCollection, boolean capsSensitive, boolean checkForInputRegion) {
        
        if (checkForInputRegion && !hasInputRegion) {
            String permittedLabel = permittedCollection[0];
            hasInputRegion = capsSensitive ? permittedLabel.equals(inputName) : permittedLabel.equalsIgnoreCase(inputName);

            if (hasInputRegion)
                return true;
        }

        for (String permitted : permittedCollection) {
            boolean isMatch = capsSensitive ? permitted.equals(inputName) : permitted.equalsIgnoreCase(inputName);
            
            if (isMatch)
                return true; 
        }

        return false; 
    }

    //MAPEAMOS LOS NOMBRES DE LOS TIPOS DE VARIABLES (de string a String, de integer a int, etc)
    private static String mapVariableTypes(String varType){

        lastTypeWasString = false;

        switch (varType) {
            case "string":
                lastTypeWasString = true;
                return UpperFirstLetter(varType);

            case "integer":
                return "int";
        
            default:
                return varType;
        }
    }

    //PONEMOS EN MAYUSCULA LA PRIMERA LETRA DE LA PALABRA
    private static String UpperFirstLetter(String word){
        if(word.isBlank() || word.isEmpty()) return "";

        String c1 = word.substring(0,1).toUpperCase();
        return c1 + word.substring(1);
    }    

    //EXCEPCION PARA XML INVALIDOS
    private static class InvalidXMLException extends Exception{
        public InvalidXMLException(String message){
            super(message);
        }
    }
}

