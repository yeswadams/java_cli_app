package  com.csv.analyzer.cli;

import com.csv.analyzer.domain.TransactionType;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommandLineParser {
    /*
    * Architecture Plan
    * 1. Safely route unknown/empty inputs directly to the HELP screen.
    * 2. Extract the physical file path and verify the minimum argument position
    * 3. Lopp through optional suffix parameters to extract filter constraints (--type, --category)
    * */

    private  Command command = Command.HELP;
    private Path filePath;
    private TransactionType filterType;
    private String filterCategory;

    public void parse(String[] args) {
        /*
        * Check for missing or empty arguments
        * If a user executes the application without flags, crashing with an ArrayIndexOutOfBoundsException
        * is unprofessional. We gracefully fall back to displaying the HELP guide
        */
        if(args == null || args.length == 0) {
            command = Command.HELP;
            return;
        }

        /*
        * Match the first terminal flag against our known cmd enums
        * handle case-sensitive texts by converting them to Caps
        * if match fails redirect them to HELP
        */

        String cmdInput = args[0].toUpperCase();
        try {
            command = Command.valueOf(cmdInput);
        } catch (IllegalArgumentException e) {
            command = Command.HELP;
            return;
        }

//        If user explicitly requests for help
        if (command == Command.HELP) return;

//        Validate file operands placement
        if(args.length < 2) {
            throw  new IllegalArgumentException("Missing target CSV file parameter path.");
        }
        filePath = Paths.get(args[1]);

        for (int i = 2; i < args.length; i++ ) {
            if ("--type".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                filterType = TransactionType.valueOf(args[++i].toUpperCase());
            } else if ("--category".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                filterCategory = args[++i];
            }
        }
    }

//    Getters allowing our Main class securely read the parsed config states

    public Command getCommand() { return command; }
    public Path getFilePath() { return filePath; }
    public TransactionType getFilterType() { return filterType; }
    public String getFilterCategory() { return filterCategory; }
}