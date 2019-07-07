import exceptions.EmptyTapeException;
import exceptions.UnstoppableMachineException;
import exceptions.WrongCommandTextFormat;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.Runtime;

public class Main {
    private static long getFileSizeBytes(File file) {
        return file.length();
    }
    public static void main(String[] args) {
        String line;
        final long maxMemory = Runtime.getRuntime().maxMemory(); //maximum amount of memory that the JVM can use

        boolean stop = false;       // остановка машины
        boolean canceled = false; //если на брейкпоинте осознали, что вывод программы ошибочен, можно отменить
        ArrayList<Short> tape = new ArrayList<>();
        ArrayList<Command> commands = new ArrayList();
        boolean unstoppableMachine = true;
        try {
            File input = new File("./src/progs/" + args[0]);
            FileReader fileReader = new FileReader(input);  ///////////////////////////////////////ОТКРЫЛ (1)
            BufferedReader br = new BufferedReader(fileReader);
            line = br.readLine();

            int address1;
            int address2;
            Command command;
            String arr[];
            while (line != null) {
                arr = line.split(" ");

                if (arr.length > 1 && !arr[1].equals("0"))
                    address1 = Short.parseShort(arr[1]);
                else address1 = 0;
                switch (arr[0]) {
                    case ">":
                        command = new Command(Type.SHIFT_RIGHT, address1, 0);
                        commands.add(command);
                        break;
                    case "<":
                        command = new Command(Type.SHIFT_LEFT, address1, 0);
                        commands.add(command);
                        break;
                    case "c":   ////очистить метку
                        command = new Command(Type.CLEAR, address1, 0);
                        commands.add(command);
                        break;

                    case "m":   ////поставить метку
                        command = new Command(Type.MARK, address1, 0);
                        commands.add(command);
                        break;

                    case "?":
                        System.out.println(line);
                        if (arr.length == 3 && !arr[2].equals("0")) {
                            address2 = Short.parseShort(arr[2]);
                            command = new Command(Type.CONDITION, address1, address2);
                            commands.add(command);
                            address2 = 0; //снова обнуляю
                        } else throw new WrongCommandTextFormat();
                        break;

                    case "!":
                        unstoppableMachine = false;
                        command = new Command(Type.STOP, 0, 0);
                        commands.add(command);
                        break;
                }
                line = br.readLine();
            }
            if (unstoppableMachine) throw new UnstoppableMachineException();

            // могу ли сделать подобное:
            //---------------------------------------------------
            StringBuilder stringBuilder = new StringBuilder();
            long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            input = new File("./src/tapes/" + args[1]);
            if(maxMemory - usedMemory < getFileSizeBytes(input)){ //если размер файла превышает допустимый
                stop = true;
                canceled = true;
                br.close();
                fileReader.close();
                throw new OutOfMemoryError();
            } else { // если размер файла удовлетворительный, то продолжаем работу
                fileReader = new FileReader(input);
                br = new BufferedReader(fileReader);
                //line = br.readLine();
                while( ( line = br.readLine() ) != null )
                    stringBuilder.append( line );

                br.close();
                fileReader.close(); ///////////////////////////////////////ЗАКРЫЛ (1)

                if (stringBuilder.toString().isEmpty()) throw new EmptyTapeException();

                for (int i = 0; i < stringBuilder.length(); i++) {
                    String letter = stringBuilder.substring(i, i + 1);
                    short number = Short.parseShort(letter);
                    tape.add(number);
                }
                //----------------------------------------------------

                for (int i = 0; i < commands.size(); i++) {
                    System.out.println(commands.get(i).getType() + " " + commands.get(i).getAddr1()
                            + " " + commands.get(i).getAddr2());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Разбор комманд

        int tape_cursor = 0;
        int command_cursor = 1;     // для обращения к массиву команд не забыть отнять единицу
        Command current_command;    // рассматриваемая в текущий момент команда

        System.out.println(tape.toString());
        int command_counter = 0;
        int command_limit = 0;

        //создание текстового файла ответа в папке results
        System.out.println("---------------------------POST-MACHINE EXECUTION---------------------------");
        if (!(args.length < 3)) command_limit = Integer.parseInt(args[2]);
        //loop:
        while (!stop && command_counter != 1000000) {
            command_counter++;

            current_command = commands.get(command_cursor - 1);
            System.out.println(current_command.getType() + " " + current_command.getAddr1()
                    + " " + current_command.getAddr2() + "       /// tape_cursor = " + tape_cursor);
            System.out.println(tape.toString());

            switch (current_command.getType()) {
                case SHIFT_RIGHT:
                    tape_cursor++;
                    if (tape_cursor == tape.size())
                        tape.add(tape_cursor, (short) 0);
                    //потому что команды находятся в массиве, где у 1-ого элемента нулевой индекс
                    if (current_command.getAddr1() == 0) command_cursor++;
                    else
                        command_cursor = current_command.getAddr1();//////////
                    break;
                case SHIFT_LEFT:
                    if (tape_cursor - 1 < 0) {
                        tape.add(0, (short) 0);
                        tape_cursor = 0;
                    } else tape_cursor--;

                    if (current_command.getAddr1() == 0) command_cursor++;
                    else
                        command_cursor = current_command.getAddr1();//////////
                    break;
                case CLEAR:
                    tape.set(tape_cursor, (short) 0);
                    //если нет перехода в команде, то вып. след.команду, иначе вып. команду под адресом addr1
                    if (current_command.getAddr1() == 0) command_cursor++;
                    else
                        command_cursor = current_command.getAddr1();//////////
                    break;
                case MARK:
                    tape.set(tape_cursor, (short) 1);
                    if (current_command.getAddr1() == 0) command_cursor++;
                    else
                        command_cursor = current_command.getAddr1(); //////////
                    break;
                case CONDITION:
                    if (tape.get(tape_cursor) == (short) 0)
                        command_cursor = current_command.getAddr1(); //if tape_cursor==0, take 1st addr //////////
                    else
                        command_cursor = current_command.getAddr2(); //if tape_cursor==1, take 2nd addr //////////
                    break;
                case STOP:
                    stop = true;
                    break;
            }
            if (command_limit != 0 && command_counter == command_limit) { //args[2] = лимит команд
                System.out.println("<<<<<<<< ATTENTION! >>>>>>>>");
                System.out.println("Command limit reached.");
                System.out.println("Last performed command is: " + current_command.getType() + " " + current_command.getAddr1()
                        + " " + current_command.getAddr2());
                System.out.println("Tape condition after the last performed command: " + tape.toString()
                        + "       /// tape_cursor = " + tape_cursor);


                System.out.println("\n(Note: 1 - cell has a mark, 0 - cell has no mark )\n");
                System.out.println("To continue, \n input \"all\" to complete the program execution without a breakpoint");
                System.out.println("input a number to complete the program execution until the next breakpoint");
                System.out.println("input \"stop\" or \"s\" to shutdown the post-machine");
                System.out.println("input \"fexit\" to force shutdown the post-machine without any output");
                Scanner sc = new Scanner(System.in);
                Pattern pattern = Pattern.compile("(^|\n)[0-9]+$");
                System.out.print("\nInput: ");
                String user_reply = sc.nextLine();
                Matcher matcher = pattern.matcher(user_reply);
                while(!(matcher.matches() && 0 < Integer.parseInt(user_reply)
                        || user_reply.equals("stop") || user_reply.equals("s")
                        || user_reply.equals("all") || user_reply.equals("fexit"))){
                    System.out.println("Invalid command or number. Input a new one.");
                    System.out.print("\nInput: ");
                    user_reply = sc.nextLine();

                }
                switch (user_reply){
                    case "stop":
                    case "s":
                        stop = true;
                        break;
                    case "fexit":
                        stop = true;
                        canceled = true;
                        break;
                    case "all":
                        command_counter = 0;
                        command_limit = 0;
                        break;
                        default:
                            command_counter = 0;
                            command_limit = Integer.parseInt(user_reply);
                }
            }
        }
        if(!canceled) { //если не отключили машину на брейкпоинте
            String output_name = args[0].substring(0, args[0].length() - 4);
            File tempFile = new File("./src/results/result_" + output_name + "_" +
                    args[1].substring(0, args[1].length() - 4) + ".txt");
            int i = 0;
            while (tempFile.exists()) {
                i++;
                tempFile = new File("./src/results/result_" + output_name + i + ".txt");
            }
            try (FileWriter writer = new FileWriter(tempFile, false)) {
                writer.write(tape.toString());
                writer.flush(); //очищу буфер вывода
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
