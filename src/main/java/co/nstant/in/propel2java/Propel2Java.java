package co.nstant.in.propel2java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Propel2Java {

    private static final Logger LOGGER = LoggerFactory.getLogger(Propel2Java.class);

    public static class Propel extends HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> {
    }

    private static String schemaFilename;
    private static String packageName;
    private static String outputDirectory;
    private static Propel schema;

    private static void parseCommandLineOptions(String[] args) {
        OptionParser parser = new OptionParser("s:p:o:");
        OptionSet options = parser.parse(args);

        if (!options.has("s") || !options.has("p") || !options.has("o")) {
            System.err.println("Usage: propel2java -s <schema> -p <package name> -o <output directory>");
            System.err.println("Example: propel2java -s schema.yml -p co.nstant.in -o /tmp/");
            System.exit(1);
        }

        schemaFilename = (String) options.valueOf("s");
        packageName = (String) options.valueOf("p");
        outputDirectory = (String) options.valueOf("o");
    }

    private static void readSchema() {
        Yaml yaml = new Yaml(new Constructor(Propel.class));
        try (FileInputStream fileInputStream = new FileInputStream(schemaFilename)) {
            schema = yaml.load(fileInputStream);
        } catch (IOException ioException) {
            LOGGER.error("readSchema", ioException);
        }
    }

    private static void generateSources() {
        for (String entityName : schema.get("propel").keySet()) {
            if (entityName.startsWith("_")) {
                continue;
            }
            generateSource(schema.get("propel").get(entityName));
        }
    }

    private static void generateSource(HashMap<String, HashMap<String, Object>> entity) {
        String className = (String) entity.get("_attributes").get("phpName");
        File javaFile = new File(outputDirectory, className + ".java");
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write("package " + packageName + ";\n");
            writer.write("\n");
            writer.write("import com.google.gson.annotations.SerializedName;\n");
            writer.write("\n");
            writer.write("@Data\n");
            writer.write("public class " + className + " {\n\n");

            for (String propertyKey : entity.keySet()) {
                if (propertyKey.startsWith("_")) {
                    continue;
                }
                HashMap<String, Object> property = entity.get(propertyKey);
                String camelCase = CaseUtils.toCamelCase(propertyKey, false, '_');

                writer.write("    // " + property + "\n");
                if (!camelCase.equals(propertyKey)) {
                    writer.write("    @SerializedName(\"" + propertyKey + "\")\n");
                }

                switch ((String) property.get("type")) {
                case "TIMESTAMP":
                    writer.write("    private DateTime " + camelCase + ";\n");
                    break;
                case "INTEGER":
                    writer.write("    private Integer " + camelCase + ";\n");
                    break;
                case "SMALLINT":
                    writer.write("    private Integer " + camelCase + ";\n");
                    break;
                case "VARCHAR":
                    writer.write("    private String " + camelCase + ";\n");
                    break;
                case "TINYINT":
                    writer.write("    private Integer " + camelCase + ";\n");
                    break;
                case "DECIMAL":
                    writer.write("    private BigDecimal " + camelCase + ";\n");
                    break;
                case "DOUBLE":
                    writer.write("    private BigDecimal " + camelCase + ";\n");
                    break;
                case "FLOAT":
                    writer.write("    private BigDecimal " + camelCase + ";\n");
                    break;
                case "TIME":
                    writer.write("    private DateTime " + camelCase + ";\n");
                    break;
                case "BIGINT":
                    writer.write("    private BigInteger " + camelCase + ";\n");
                    break;
                case "LONGVARCHAR":
                    writer.write("    private String " + camelCase + ";\n");
                    break;
                default:
                    LOGGER.error("Unknown type: {}", property.get("type"));
                    break;
                }
                writer.write("\n");
            }

            writer.write("}\n\n");
        } catch (IOException ioException) {
            LOGGER.error("generateSource", ioException);
        }
    }

    public static void main(String[] args) {
        parseCommandLineOptions(args);
        readSchema();
        generateSources();
    }

}
