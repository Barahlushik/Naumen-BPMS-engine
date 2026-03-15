package ru.naumen.bpms.cli;

import org.springframework.stereotype.Component;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.service.ProcessService;

// TODO: Использовать паттерн команда
@Component
public class CommandProcessor {

    private final ProcessService processService;

    public CommandProcessor(ProcessService processService) {
        this.processService = processService;
    }

    public void processCommand(String input) {

        String[] cmd = input.trim().split(" ");

        if (cmd.length == 0 || cmd[0].isBlank()) {
            System.out.println("Введите команду. Используйте 'help' для справки.");
            return;
        }

        try {

            switch (cmd[0]) {

                case "help" -> printHelp();

                case "create" -> {

                    if (cmd.length < 3) {
                        System.out.println("Использование: create <title> <description>");
                        return;
                    }

                    ProcessDefinition process = new ProcessDefinition();
                    process.setTitle(cmd[1]);
                    process.setDescription(cmd[2]);

                    processService.createProcess(process);

                    System.out.println("Процесс успешно создан.");
                }

                case "get" -> {

                    if (cmd.length < 2) {
                        System.out.println("Использование: get <id>");
                        return;
                    }

                    Long id = Long.parseLong(cmd[1]);

                    ProcessDefinition process = processService.getProcess(id);

                    System.out.println(process);
                }

                case "update" -> {

                    if (cmd.length < 4) {
                        System.out.println("Использование: update <id> <title> <description>");
                        return;
                    }

                    Long id = Long.parseLong(cmd[1]);

                    ProcessDefinition process = new ProcessDefinition();
                    process.setId(id);
                    process.setTitle(cmd[2]);
                    process.setDescription(cmd[3]);

                    processService.updateProcess(process);

                    System.out.println("Процесс обновлён.");
                }

                case "delete" -> {

                    if (cmd.length < 2) {
                        System.out.println("Использование: delete <id>");
                        return;
                    }

                    Long id = Long.parseLong(cmd[1]);

                    processService.deleteProcess(id);

                    System.out.println("Процесс удалён.");
                }

                default -> System.out.println("Неизвестная команда. Введите 'help' для списка команд.");
            }

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void printHelp() {

        System.out.println("""
                
                Доступные команды:
                
                help
                    Показать список команд
                
                create <title> <description>
                    Создать процесс
                
                get <id>
                    Получить процесс по id
                
                update <id> <title> <description>
                    Обновить процесс
                
                delete <id>
                    Удалить процесс
                
                exit
                    Выход из программы
                
                """);
    }
}