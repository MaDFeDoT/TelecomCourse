package DBHandler;

import java.sql.*;
import java.util.Properties;

public class DB
{
    public String DB_URL = "jdbc:firebirdsql://localhost:3050/C:/IMAP4/DB.FDB";
    public String DB_DEFAULT_USER = "SYSDBA";
    public String DB_DEFAULT_PASSWORD = "masterkey";
    public String DB_DEFAULT_ENCODING = "win1251";
    public Connection conn = null;
    Properties props = null;

    /**
     * Конструктор DB
     * заполняем параметры, вызываем подключение
     * @throws ClassNotFoundException - ошибка jdbc
     */
    public DB() throws ClassNotFoundException
    {
        Class.forName("org.firebirdsql.jdbc.FBDriver");
        props = new Properties();
        props.setProperty("user", DB_DEFAULT_USER);
        props.setProperty("password", DB_DEFAULT_PASSWORD);
        props.setProperty("encoding", DB_DEFAULT_ENCODING);
        DBConnect();
    }

    /**
     * Подключение к БД
     */
    public void DBConnect()
    {
        try {
            conn = DriverManager.getConnection(DB_URL, props);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Отключение от БД
     */
    public void BDDisconnect()
    {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Поиск пользователя
     * @param login
     * @param password
     * @return resultSet
     */
    public ResultSet FindUser (String login, String password)
    {
        String Query = "SELECT USER_ID AS \"USER_ID\"" +
                " FROM USERS" +
                " WHERE LOGIN  = '" +  login + "' AND PASSWORD = '" + password + "'";
        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            ResultSet resultSet = statement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Считаем количество писем в конкретной папке
     * @param user_id - ID пользователя
     * @param folder - имя папки
     * @return resultSet
     */
    public ResultSet CountExistMessages(String user_id, String folder)
    {
        String Query = "SELECT COUNT(1) AS \"COUNT\" FROM MESSAGES WHERE USER_ID = '" + user_id + "'" +
                " AND FOLDER = '" + folder + "'";

        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            ResultSet resultSet = statement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Считаем недавние сообщения в папке
     * @param user_id - ID пользователя
     * @param folder - имя папки
     * @return resultSet
     */
    public ResultSet CountRecentMessages (String user_id, String folder)
    {
        String Query = "SELECT COUNT(1) AS \"COUNT\" FROM MESSAGES WHERE USER_ID = '" + user_id + "'" +
            " AND FLAG_RECENT = 1" +  " AND FOLDER = '" + folder + "'";

        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            ResultSet resultSet = statement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Считаем непрочитанные сообщения в папке
     * @param user_id - ID пользователя
     * @param folder - имя папки
     * @return resultSet
     */
    public ResultSet CountUnSeenMessages (String user_id, String folder)
    {
        String Query = "SELECT COUNT(1) AS \"COUNT\" FROM MESSAGES WHERE USER_ID = '" + user_id + "'" +
                " AND FLAG_SEEN = 0" + " AND FOLDER = '" + folder + "'";
        //System.out.println(Query);
        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            ResultSet resultSet = statement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получаем ID первого непрочитанного сообщения
     * @param user_id - ID пользователя
     * @param folder - имя папки
     * @return resultSet
     */
    public ResultSet FirstUnseen (String user_id, String folder)
    {
        String Query = "SELECT MESSAGE_ID AS \"MESSAGE_ID\" FROM MESSAGES WHERE USER_ID = '" + user_id + "'" +
                " AND FLAG_SEEN = 0" + " AND FOLDER = '" + folder + "'" +
                " ORDER BY MESSAGE_ID ASCENDING";
        System.out.println(Query);
        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            ResultSet resultSet = statement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получение флагов сообщения
     * @param user_id - ID пользователя
     * @param folder - имя папки
     * @param message_id - ID сообщения
     * @return resultSet
     */
    public ResultSet GetMessageFlags (String user_id, String folder, String message_id)
    {
        String Query = "SELECT FLAG_SEEN AS \"SEEN\"," +
                " FLAG_ANSWERED AS \"ANSWERED\"," +
                " FLAG_DELETED AS \"DELETED\"," +
                " FLAG_DRAFT AS \"DRAFT\"," +
                " FLAG_RECENT AS \"RECENT\"" +
                " FROM MESSAGES WHERE USER_ID = '" + user_id + "'" +
                " AND MESSAGE_ID ='" + message_id + "'" +
                " AND FOLDER = '" + folder + "'";
        //System.out.println(Query);
        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            ResultSet resultSet = statement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Считаем количество сообщений с флагом в определенной папке
     * @param user_id - ID пользователя
     * @param folder - - имя папки
     * @param flag - флаг
     * @return resultSet
     */
    public ResultSet CountFlageddMessages (String user_id, String folder, String flag)
    {
        String Query = "SELECT MESSAGE_ID AS \"MESSAGE_ID\" FROM MESSAGES WHERE USER_ID = '" + user_id + "'" +
                " AND FLAG_" + flag +" = 1" + " AND FOLDER = '" + folder + "'";
        //System.out.println(Query);
        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            ResultSet resultSet = statement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Получение вложенных папок
     * @param user_id - ID пользователя
     * @param folder - имя папки
     * @return resultSet
     */
    public ResultSet GetDeepFolser (String user_id, String folder)
    {
        String Query = "SELECT FOLDERS.FOLDER_NAME AS \"FOLDER_NAME\" FROM FOLDERS" +
                " LEFT OUTER JOIN USERS" +
                " ON FOLDERS.USER_ID  = USERS.USER_ID" +
                " WHERE FOLDERS.UPPER_FOLDER_NAME = '" + folder + "'" +
                " AND FOLDERS.USER_ID = '" + user_id + "'";
        //System.out.println(Query);
        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            ResultSet resultSet = statement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Поиск папки
     * @param user_id - ID пользователя
     * @param folder - имя папки
     * @return resultSet
     */
    public ResultSet SearchFolder (String user_id, String folder)
    {
        String Query = "SELECT FOLDERS.FOLDERS_ID AS \"FOLDERS_ID\" FROM FOLDERS" +
                " LEFT OUTER JOIN USERS" +
                " ON FOLDERS.USER_ID  = USERS.USER_ID" +
                " WHERE FOLDERS.FOLDER_NAME = '" + folder + "'" +
                " AND FOLDERS.USER_ID = '" + user_id + "'";
        //System.out.println(Query);
        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            ResultSet resultSet = statement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Генерируем новое ID для сообщения
     * @return resultSet
     */
    public ResultSet GetMessageUid ()
    {
        String Query = "SELECT GEN_ID(GEN_MESSAGES_ID, 0) FROM RDB$DATABASE";
        //System.out.println(Query);
        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            ResultSet resultSet = statement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Добавляем новый флаг для сообщения
     * @param message_id - ID сообщения
     * @param flag - имя флага
     * @return 1 - если успешно
     *         0 - если не успешно
     */
    public int AddFlagToMessage (String message_id, String flag)
    {
        String Query = "UPDATE MESSAGES SET FLAG_" + flag + " = '1'" +
                " WHERE MESSAGE_ID = '" + message_id + "'";
        //System.out.println(Query);
        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            //return statement.execute();
            return  statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Удаление флага из сообщения
     * @param message_id - ID сообщений
     * @param flag - имя флага
     * @return 1 - если успешно
     *         0 - если не успешно
     */
    public int RemoveFlagFromMessage (String message_id, String flag)
    {
        String Query = "UPDATE MESSAGES SET FLAG_" + flag + " = '0'" +
                " WHERE MESSAGE_ID = '" + message_id + "'";
        //System.out.println(Query);
        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            //return statement.execute();
            return  statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Добавление папки
     * @param user_id - ID пользователя
     * @param folder - имя папки
     * @return 1 - если успешно
     *         0 - если не успешно
     */
    public int AddFolderToUser (String user_id, String folder)
    {
        String Query = "INSERT INTO FOLDERS (FOLDER_NAME, USER_ID, FOLDERS_ID)" +
                " VALUES ('" + folder + "', '" + user_id + "', (SELECT GEN_ID(GEN_FOLDERS_ID, 1) FROM RDB$DATABASE))";
        //System.out.println(Query);
        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            return  statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Копирование сообщения в папку
     * @param message_id - ID сообщения
     * @param folder - имя папки назначения
     * @return 1 - если успешно
     *         0 - если не успешно
     */
    public int CopyMessage (String message_id, String folder)
    {
        String Query = "UPDATE MESSAGES SET FOLDER = '" + folder + "'" +
                " WHERE MESSAGE_ID = '" + message_id + "'";
        System.out.println(Query);
        try {
            PreparedStatement statement = conn.prepareStatement(Query);
            //return statement.execute();
            return  statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
