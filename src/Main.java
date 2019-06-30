import exceptions.EmptyTapeException;
import exceptions.UnstoppableMachineException;
import exceptions.WrongCommandTextFormat;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        String line;

        ArrayList<Short> tape = new ArrayList<>();
        ArrayList<Command> commands = new ArrayList();
        boolean unstoppableMachine = true;
        try{
            File input = new File("./src/progs/" + args[0]);
            FileReader fileReader = new FileReader(input);  ///////////////////////////////////////ОТКРЫЛ (1)
            BufferedReader br = new BufferedReader(fileReader);
            line = br.readLine();

            Pattern pattern = Pattern.compile("-?//%d+");
            int address1;
            int address2;
            Command command;
            String arr[];
            while (line != null){
                arr = line.split(" ");
                /*
                Matcher matcher = pattern.matcher(line);
                if(matcher.find())
                    address1 = Short.parseShort(matcher.group());
                else
                    address1 = 0;
                */
                if(arr.length > 1 && !arr[1].equals("0"))
                    address1 = Short.parseShort(arr[1]);
                else address1 = 0;
                //switch (line.substring(0, 1)){
                switch (arr[0]){
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
                        /*
                        if (matcher.find()) {
                            address2 = Short.parseShort(matcher.group());
                            command = new Command(Type.CONDITION, address1, address2);
                            commands.add(command);
                            address2 = 0; //снова обнуляю
                        } else throw new WrongCommandTextFormat();*/
                        if(arr.length == 3 && !arr[2].equals("0")){
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

            input = new File("./src/tapes/" + args[1]);
            fileReader = new FileReader(input);
            br = new BufferedReader(fileReader);
            line = br.readLine();

            br.close();
            fileReader.close(); ///////////////////////////////////////ЗАКРЫЛ (1)

            if (line == null) throw new IOException();

            for(int i = 0; i < line.length(); i++){
                String letter = line.substring(i, i + 1);
                short number = Short.parseShort(letter);
                tape.add(number);
            }
            //----------------------------------------------------

            for (int i = 0; i < commands.size(); i++){
                System.out.println(commands.get(i).getType() + " " + commands.get(i).getAddr1()
                        + " " + commands.get(i).getAddr2());
            }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Разбор комманд
        boolean stop = false;       // останов
        int tape_cursor = 0;
        int command_cursor = 1;     // для обращения к массиву команд не забыть отнять единицу
        Command current_command;    // рассматриваемая в текущий момент команда

        if (tape == null) throw new EmptyTapeException();
        System.out.println(tape.toString());
        while (!stop){
            current_command = commands.get(command_cursor);
            System.out.println(current_command.getType() + " " + current_command.getAddr1()
                    + " " + current_command.getAddr2() + "       /// tape_cursor = " +  tape_cursor);
            System.out.println(tape.toString());

            switch (current_command.getType()){
                case SHIFT_RIGHT:
                    tape_cursor++;
                    if(tape_cursor == tape.size())
                        tape.add(tape_cursor ,(short) 0);
                    //потому что команды находятся в массиве, где у 1-ого элемента нулевой индекс
                    if(current_command.getAddr1() == 0) command_cursor++;
                    else
                        command_cursor = current_command.getAddr1() - 1;
                    break;
                case SHIFT_LEFT:
                    if(tape_cursor - 1 < 0){
                        tape.add(0,(short) 0);
                        tape_cursor = 0;
                    } else tape_cursor--;

                    if(current_command.getAddr1() == 0) command_cursor++;
                    else
                        command_cursor = current_command.getAddr1() - 1;
                    break;
                case CLEAR:
                    tape.set(tape_cursor, (short) 0);
                    //если нет перехода в команде, то вып. след.команду, иначе вып. команду под адресом addr1
                    if(current_command.getAddr1() == 0) command_cursor++;
                    else
                        command_cursor = current_command.getAddr1() - 1;
                    break;
                case MARK:
                    tape.set(tape_cursor, (short) 1);
                    if(current_command.getAddr1() == 0) command_cursor++;
                    else
                        command_cursor = current_command.getAddr1() - 1;
                    break;
                case CONDITION:
                    if (tape.get(tape_cursor) == (short) 0)
                        command_cursor = current_command.getAddr1() - 1; //if tape_cursor==0, take 1st addr
                    else
                        command_cursor = current_command.getAddr2() - 1; //if tape_cursor==1, take 2nd addr
                    break;
                case STOP:
                    stop = true;
                    break;
            }
        }
        String output_name = args[0].substring(0, args[0].length() - 4);
        System.out.println(output_name);
        File tempFile = new File("./src/results/result_" + output_name + ".txt");
        int i = 0;
        while (tempFile.exists()){
            i++;
            tempFile = new File("./src/results/result_" + output_name + i + ".txt");
        }
    //try(FileWriter writer = new FileWriter("./src/result_" + output_name + i + ".txt", false)){
        try (FileWriter writer = new FileWriter(tempFile, false)){
        writer.write(tape.toString());
        writer.flush(); //очищу буфер вывода
    } catch (IOException e){
        e.printStackTrace();
        }
    }
}
