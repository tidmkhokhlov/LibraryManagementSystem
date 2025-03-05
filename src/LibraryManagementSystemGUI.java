import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.util.Properties;

public class LibraryManagementSystemGUI {
    // Параметры подключения к системной базе данных (для создания и удаления базы данных)
    private static final String SYSTEM_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static String PASSWORD; // Пароль будет загружен из файла

    // Параметры подключения к базе данных Library (для всех остальных операций)
    private static final String LIBRARY_URL = "jdbc:postgresql://localhost:5432/library";

    // Текущая роль пользователя (по умолчанию "гость")
    private static String currentRole = "guest";

    // Статический блок для загрузки пароля из файла config.properties
    static {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            Properties props = new Properties();
            props.load(fis);
            PASSWORD = props.getProperty("db.password"); // Загружаем пароль из файла
        } catch (Exception ex) {
            // Если произошла ошибка при загрузке пароля, выводим сообщение и завершаем программу
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке пароля: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Завершаем программу с кодом ошибки 1
        }
    }

    // Главный метод программы
    public static void main(String[] args) {
        // Выбор роли при запуске программы
        String[] roles = {"Администратор", "Модератор", "Гость"};
        int roleChoice = JOptionPane.showOptionDialog(
                null,
                "Выберите роль:",
                "Выбор роли",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                roles,
                roles[2] // По умолчанию выбрана роль "Гость"
        );

        // Устанавливаем текущую роль в зависимости от выбора пользователя
        if (roleChoice == 0) {
            currentRole = "admin"; // Администратор
        } else if (roleChoice == 1) {
            currentRole = "moderator"; // Модератор
        } else {
            currentRole = "guest"; // Гость
        }

        // Проверка подключения к базе данных при запуске программы
        try (Connection conn = getLibraryConnection(currentRole)) {
            if (conn != null) {
                System.out.println("Connected to the database as " + currentRole);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to connect to the database!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Создаем главное окно приложения
        JFrame frame = new JFrame("Управление базой данных библиотеки");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Закрытие программы при закрытии окна
        frame.setSize(300, 400); // Устанавливаем размер окна

        // Центрируем окно на экране
        frame.setLocationRelativeTo(null);

        // Создаем панель для размещения компонентов (кнопок)
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout()); // Используем GridBagLayout для центрирования компонентов
        frame.add(panel);

        // Добавляем кнопки на панель
        placeButtons(panel);

        // Делаем окно видимым
        frame.setVisible(true);
    }

    // Метод для размещения кнопок на панели
    private static void placeButtons(JPanel panel) {
        // Настройки для GridBagConstraints (для управления расположением компонентов)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Каждый компонент занимает всю строку
        gbc.fill = GridBagConstraints.HORIZONTAL; // Растягиваем компоненты по горизонтали
        gbc.insets = new Insets(2, 0, 2, 0); // Отступы сверху и снизу по 2 пикселя

        // Создаем кнопки и добавляем их на панель
        JButton createDatabaseButton = createButton("Create Database", panel, gbc);
        JButton deleteDatabaseButton = createButton("Delete Database", panel, gbc);
        JButton clearTableButton = createButton("Clear Table", panel, gbc);
        JButton addRecordButton = createButton("Add Record", panel, gbc);
        JButton searchRecordButton = createButton("Search Record", panel, gbc);
        JButton updateRecordButton = createButton("Update Record", panel, gbc);
        JButton deleteRecordButton = createButton("Delete Record", panel, gbc);
        JButton viewAllButton = createButton("View All", panel, gbc);

        // Обработчики событий для каждой кнопки
        createDatabaseButton.addActionListener(e -> {
            if (currentRole.equals("admin")) {
                createDatabase(); // Только администратор может создавать базу данных
            } else {
                JOptionPane.showMessageDialog(panel, "Доступ запрещен! Только администратор может создавать базу данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteDatabaseButton.addActionListener(e -> {
            if (currentRole.equals("admin")) {
                deleteDatabase(); // Только администратор может удалять базу данных
            } else {
                JOptionPane.showMessageDialog(panel, "Доступ запрещен! Только администратор может удалять базу данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        clearTableButton.addActionListener(e -> {
            if (currentRole.equals("admin") || currentRole.equals("moderator")) {
                clearTable(); // Администратор и модератор могут очищать таблицу
            } else {
                JOptionPane.showMessageDialog(panel, "Доступ запрещен! Только администратор или модератор могут очищать таблицу.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        addRecordButton.addActionListener(e -> {
            if (currentRole.equals("admin") || currentRole.equals("moderator")) {
                addRecord(panel); // Администратор и модератор могут добавлять записи
            } else {
                JOptionPane.showMessageDialog(panel, "Доступ запрещен! Только администратор или модератор могут добавлять записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        searchRecordButton.addActionListener(e -> searchRecord(panel)); // Поиск доступен всем

        updateRecordButton.addActionListener(e -> {
            if (currentRole.equals("admin") || currentRole.equals("moderator")) {
                updateRecord(panel); // Администратор и модератор могут обновлять записи
            } else {
                JOptionPane.showMessageDialog(panel, "Доступ запрещен! Только администратор или модератор могут обновлять записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteRecordButton.addActionListener(e -> {
            if (currentRole.equals("admin") || currentRole.equals("moderator")) {
                deleteRecord(panel); // Администратор и модератор могут удалять записи
            } else {
                JOptionPane.showMessageDialog(panel, "Доступ запрещен! Только администратор или модератор могут удалять записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        viewAllButton.addActionListener(e -> viewAllRecords(panel)); // Просмотр всех записей доступен всем
    }

    // Метод для создания кнопки и добавления ее на панель
    private static JButton createButton(String text, JPanel panel, GridBagConstraints gbc) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(200, 30)); // Устанавливаем одинаковый размер для всех кнопок
        panel.add(button, gbc); // Добавляем кнопку на панель
        return button;
    }

    // Метод для подключения к системной базе данных (postgres)
    private static Connection getSystemConnection() throws SQLException {
        return DriverManager.getConnection(SYSTEM_URL, USER, PASSWORD);
    }

    // Метод для подключения к базе данных Library с учетом роли
    private static Connection getLibraryConnection(String role) throws SQLException {
        String url = LIBRARY_URL;
        String user = role; // Используем роль как имя пользователя
        String password;

        // В зависимости от роли выбираем соответствующий пароль
        switch (role) {
            case "admin":
                password = "admin_password";
                break;
            case "moderator":
                password = "moderator_password";
                break;
            case "guest":
                password = "guest_password";
                break;
            default:
                throw new IllegalArgumentException("Неизвестная роль: " + role);
        }

        return DriverManager.getConnection(url, user, password);
    }

    // Метод для создания базы данных
    private static void createDatabase() {
        try (Connection connSys = getSystemConnection()) {
            // Выполнение SQL-запроса для создания базы данных
            try (Statement stmt = connSys.createStatement()) {
                stmt.execute("CREATE DATABASE Library;");
                JOptionPane.showMessageDialog(null, "База данных создана успешно!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Ошибка при создании базы данных: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        try (Connection connLib = getLibraryConnection(currentRole)) {
            // Чтение SQL-кода из файла sample.sql
            String sql = new String(Files.readAllBytes(Paths.get("samples.sql")));

            // Выполнение SQL-кода для создания таблиц и процедур
            try (Statement stmt = connLib.createStatement()) {
                stmt.execute(sql); // Выполнение SQL-запроса
                JOptionPane.showMessageDialog(null, "Процедуры созданы успешно!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Ошибка при создании процедур: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для удаления базы данных
    private static void deleteDatabase() {
        try (Connection conn = getSystemConnection()) {
            // Выполнение SQL-запроса для удаления базы данных
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP DATABASE IF EXISTS Library;");
                JOptionPane.showMessageDialog(null, "База данных удалена успешно!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Ошибка при удалении базы данных: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для очистки таблицы
    private static void clearTable() {
        try (Connection conn = getLibraryConnection(currentRole)) {
            // Вызов хранимой процедуры ClearTable
            try (CallableStatement stmt = conn.prepareCall("CALL ClearTable()")) {
                stmt.execute();
                JOptionPane.showMessageDialog(null, "Таблица успешно очищена!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Ошибка при очистке таблицы: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для добавления записи
    private static void addRecord(JPanel panel) {
        String title = JOptionPane.showInputDialog("Введите название книги:");
        String author = JOptionPane.showInputDialog("Введите автора книги:");
        String yearStr = JOptionPane.showInputDialog("Введите год издания книги:");

        if (title != null && author != null && yearStr != null && !yearStr.isEmpty()) {
            try {
                int year = Integer.parseInt(yearStr);
                try (Connection conn = getLibraryConnection(currentRole)) {
                    // Вызов хранимой процедуры AddBook
                    try (CallableStatement stmt = conn.prepareCall("CALL AddBook(?, ?, ?)")) {
                        stmt.setString(1, title);
                        stmt.setString(2, author);
                        stmt.setInt(3, year);
                        stmt.execute();
                        JOptionPane.showMessageDialog(panel, "Книга успешно добавлена!");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(panel, "Ошибка при добавлении книги: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Некорректный год издания!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(panel, "Все поля должны быть заполнены!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для поиска записи
    private static void searchRecord(JPanel panel) {
        // Всплывающее окно для выбора поля поиска
        String[] options = {"Title", "Author", "Year"};
        int choice = JOptionPane.showOptionDialog(
                panel,
                "Выберите поле для поиска:",
                "Поиск книги",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0] // По умолчанию выбрана первая кнопка (Title)
        );

        // Если пользователь нажал "Отмена" или закрыл окно, завершаем метод
        if (choice == -1) {
            return;
        }

        // Получаем выбранное поле для поиска
        String searchField = options[choice].toLowerCase(); // title, author или year

        // Запрашиваем у пользователя значение для поиска
        String searchValue = JOptionPane.showInputDialog("Введите значение для поиска:");
        if (searchValue == null || searchValue.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Введите значение для поиска!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Выполняем поиск в зависимости от выбранного поля
        try (Connection conn = getLibraryConnection(currentRole)) {
            String sql;
            switch (searchField) {
                case "title":
                    sql = "SELECT * FROM FindBookByTitle(?)";
                    break;
                case "author":
                    sql = "SELECT * FROM FindBookByAuthor(?)";
                    break;
                case "year":
                    sql = "SELECT * FROM FindBookByYear(?)";
                    break;
                default:
                    throw new IllegalArgumentException("Неизвестное поле для поиска: " + searchField);
            }

            // Вызов соответствующей функции для поиска
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, searchValue);
                ResultSet rs = stmt.executeQuery();

                StringBuilder result = new StringBuilder();
                while (rs.next()) {
                    result.append("ID: ").append(rs.getInt("id")).append(" ")
                            .append("Название: ").append(rs.getString("title")).append(" ")
                            .append("Автор: ").append(rs.getString("author")).append(" ")
                            .append("Год: ").append(rs.getInt("year")).append("\n");
                }

                if (result.length() > 0) {
                    JOptionPane.showMessageDialog(panel, result.toString(), "Результат поиска", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(panel, "Книга не найдена!", "Информация", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(panel, "Ошибка при поиске книги: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(panel, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для обновления записи
    private static void updateRecord(JPanel panel) {
        String idStr = JOptionPane.showInputDialog("Введите ID книги для обновления:");
        if (idStr != null && !idStr.isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                String newTitle = JOptionPane.showInputDialog("Введите новое название книги:");
                String newAuthor = JOptionPane.showInputDialog("Введите нового автора книги:");
                String newYearStr = JOptionPane.showInputDialog("Введите новый год издания книги:");

                if (newTitle != null && newAuthor != null && newYearStr != null && !newYearStr.isEmpty()) {
                    try {
                        int newYear = Integer.parseInt(newYearStr);
                        try (Connection conn = getLibraryConnection(currentRole)) {
                            // Вызов хранимой процедуры UpdateBook
                            try (CallableStatement stmt = conn.prepareCall("CALL UpdateBook(?, ?, ?, ?)")) {
                                stmt.setInt(1, id);
                                stmt.setString(2, newTitle);
                                stmt.setString(3, newAuthor);
                                stmt.setInt(4, newYear);
                                stmt.execute();
                                JOptionPane.showMessageDialog(panel, "Книга обновлена успешно!");
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(panel, "Ошибка при обновлении книги: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(panel, "Некорректный год издания!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(panel, "Все поля должны быть заполнены!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Некорректный ID!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(panel, "Введите ID книги!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для удаления записи
    private static void deleteRecord(JPanel panel) {
        String title = JOptionPane.showInputDialog("Введите название книги для удаления:");
        if (title != null && !title.isEmpty()) {
            try (Connection conn = getLibraryConnection(currentRole)) {
                // Вызов хранимой процедуры DeleteBookByTitle
                try (CallableStatement stmt = conn.prepareCall("CALL DeleteBookByTitle(?)")) {
                    stmt.setString(1, title);
                    stmt.execute();
                    JOptionPane.showMessageDialog(panel, "Книга удалена успешно!");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Ошибка при удалении книги: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(panel, "Введите название книги!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Метод для просмотра всех записей в таблице
    private static void viewAllRecords(JPanel panel) {
        try (Connection conn = getLibraryConnection(currentRole)) {
            // SQL-запрос для получения всех записей из таблицы Book
            String sql = "SELECT * FROM ViewAllRecords()";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);

                StringBuilder result = new StringBuilder();
                while (rs.next()) {
                    result.append("ID: ").append(rs.getInt("id")).append(" ")
                            .append("Название: ").append(rs.getString("title")).append(" ")
                            .append("Автор: ").append(rs.getString("author")).append(" ")
                            .append("Год: ").append(rs.getInt("year")).append("\n");
                }

                if (result.length() > 0) {
                    JOptionPane.showMessageDialog(panel, result.toString(), "Все записи", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(panel, "Таблица пуста!", "Информация", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(panel, "Ошибка при получении данных: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}