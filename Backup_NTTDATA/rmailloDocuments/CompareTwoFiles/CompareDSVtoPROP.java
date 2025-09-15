import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class CompareDSVtoPROP{

    private static class DSV_Content{
        private LinkedHashMap<String, String> dsvAllContent;
        private HashSet<String> dsvResult;

        public DSV_Content(LinkedHashMap<String, String> dsvAllContent, HashSet<String> dsvResult){
            this.dsvAllContent = dsvAllContent;
            this.dsvResult = dsvResult;
        }

        public LinkedHashMap<String, String> getAllContent(){return dsvAllContent;}
        public HashSet<String> getDSVCodes(){return dsvResult;}
    }

    private static String propPrefix;
    private static final String FILTER_ALL = "all";
    
    //en el arg0 entra el DSV y el arg1 entra el archivo .properties el arg3 para el idioma
    public static void main(String[] args){ 
        
        if (args.length < 4) {
            System.err.println("Usage: \n- arg0 --> .dsv file \n- arg1 --> .properties file \n- arg2 --> Lenguage Code (ex: 08) "+
            "\n- arg3 --> Code Letters Filter (ex: DV) | tpye \"ALL\" to filter all codes");
            System.exit(1);
        }

        //File Paths
        String dsvFilePath = args[0];
        String propFilePath = args[1];
        String lenguageCode = args[2];
        String codeLetters = args[3];
        
        try{
            //Lista con el contenido formateado de cada archivo
            DSV_Content dsvResult = FormatDSVFile(dsvFilePath, lenguageCode, codeLetters);
            HashSet<String> propResult = FormatPROPFile(propFilePath, codeLetters);

            //Creamos un archivo con el resultado
            WriteResult(dsvResult, propResult);
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static DSV_Content FormatDSVFile(String filePath, String lenguageCode, String codeLetters){

        LinkedHashMap<String, String> dsvMap = new LinkedHashMap<>();
        HashSet<String> codeResults = new LinkedHashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line, noSpaces;
            String[] splittedParts;

            File missingLinesFile = new File("DSV_CODES_" + lenguageCode + ".txt");
            missingLinesFile.createNewFile();
            FileWriter missingLinesWrites = new FileWriter(missingLinesFile);

            while ((line = br.readLine()) != null) {
                //QUITAMOS LOS ESPACIOS
                noSpaces = line.trim();

                //SEPARAMOS POR REGIONES
                splittedParts = noSpaces.split("\\|");

                //PILLAMOS EL CODIGO Y DESCRIPCION POR SEPARADO
                String code = splittedParts[0].replaceAll("\\s+", "");
                String desc = "=" + splittedParts[2];

                if(splittedParts[1].equals(lenguageCode)){
                    dsvMap.put(code, desc);

                    if(CHeckIfRightCodeLetters(codeLetters, code)){
                        codeResults.add(code);
                        missingLinesWrites.write(line + "\n");
                    }
                }
            }

            missingLinesWrites.close();
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        return new DSV_Content(dsvMap, codeResults);
    }


    private static HashSet<String> FormatPROPFile(String filePath, String codeLetters){

        HashSet<String> propSet = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            boolean setPrefix = true;
            int substringIndex = 0;
            String line, noSpace;

            while ((line = br.readLine()) != null) {

                //PILLAMOS EL PREFIJO PARA LUEGO ESCRIBIRLO EN EL ARCHIVO DE SALIDA
                if(setPrefix){
                    setPrefix = false;
                    substringIndex = SetPrefixString(line);
                }
                
                //QUITAMOS LOS ESPACIOS RESTANTES
                noSpace = line.trim();
                propSet.add(noSpace.substring(substringIndex, noSpace.indexOf("=")));
            }
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        return propSet;
    }

    private static int SetPrefixString(String line){
        int lastDotIndex = line.lastIndexOf('.');

        propPrefix = line.substring(0, lastDotIndex + 1);
        return lastDotIndex + 1;
    }

    private static boolean CHeckIfRightCodeLetters(String codeLetters, String actualCode){

        //FILTRAMOS POR LAS LETRAS DEL CODIGO
        if(codeLetters.toLowerCase().equals(FILTER_ALL)) return true;

        String actualCodeLetters = actualCode.substring(0, 2).toLowerCase();
        return actualCodeLetters.equals(codeLetters.toLowerCase());
    }

    private static void WriteResult(DSV_Content dsvMap, HashSet<String> propSet){
        if (!propSet.isEmpty()) {

            int missingLines = 0;

            try{
                //CREAMOS UN TXT PARA LAS MISSING LINES
                File missingLinesFile = new File("MissingLines.txt");
                missingLinesFile.createNewFile();
                FileWriter missingLinesWrites = new FileWriter(missingLinesFile);

                //CREAMOS SEGUNDO TXT PARA EL NUEVO PROPERTIES
                File newPropFile = new File("newProperties.txt");
                newPropFile.createNewFile();
                FileWriter newPropWriter = new FileWriter(newPropFile);

                //LE AÑADIMOS EL CONTENIDO
                for (String code : dsvMap.getAllContent().keySet()){

                    String resultLine = propPrefix + code + dsvMap.getAllContent().get(code) + "\n";

                    if(propSet.contains(code)){
                        newPropWriter.write(resultLine);
                    }
                    else if(dsvMap.getDSVCodes().contains(code)){
                        newPropWriter.write(resultLine);
                        missingLinesWrites.write(resultLine);
                        missingLines++;
                    }
                }

                missingLinesWrites.close();
                newPropWriter.close();
                System.out.println("Added " + missingLines + " missing lines.");
                System.out.println("New .PROPERTIES file created at: " + newPropFile.getAbsolutePath());
                System.out.println("MissingLines.txt created at: " + newPropFile.getAbsolutePath());
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}