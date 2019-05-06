package com.lenis0012.bukkit.loginsecurity.modules.storage;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class MigrationRunner implements Runnable {
    private final LoginSecurity loginSecurity;
    private final DataSource dataSource;
    private final String platform;

    public MigrationRunner(LoginSecurity loginSecurity, DataSource dataSource, String platform) {
        this.loginSecurity = loginSecurity;
        this.dataSource = dataSource;
        this.platform = platform;
    }

    @Override
    public void run() {
        try(Connection connection = dataSource.getConnection()) {
            final boolean originAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                final boolean installed = isInstalled(connection);
                for(String migrationFileName : readMigrations()) {
                    final String[] migrationData = migrationFileName.split(Pattern.quote("__"));
                    final String version = migrationData[0];
                    String name = migrationData[1].replace("_", " ");
                    name = name.substring(0, name.length() - ".sql".length()); // Remove extension

                    if(!installed || !isMigrationInstalled(connection, version)) {
                        loginSecurity.getLogger().log(Level.INFO, "Applying database upgrade " + version + ": " + name);
                        final String content = getContent("sql/" + platform + "/" + migrationFileName);
                        try(Statement statement = connection.createStatement()) {
                            for(String query : content.split(";")) {
                                if(query.trim().isEmpty()) continue;
                                statement.executeUpdate(query);
                            }
                            insertMigration(connection, version, name);
                            connection.commit();
                        }
                    }
                }
            } finally {
                connection.setAutoCommit(originAutoCommit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertMigration(Connection connectionm, String version, String name) throws SQLException {
        try(PreparedStatement statement = connectionm.prepareStatement(
                "INSERT INTO ls_upgrades (version, description, applied_at) VALUES (?,?,?);")) {
            statement.setString(1, version);
            statement.setString(2, name);
            statement.setTimestamp(3, Timestamp.from(Instant.now()));
            statement.executeUpdate();
        }
    }

    private boolean isMigrationInstalled(Connection connection, String version) throws SQLException {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT version FROM ls_upgrades WHERE version=?;")) {
            statement.setString(1, version);
            try(ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private boolean isInstalled(Connection connection) throws SQLException {
        final DatabaseMetaData metaData = connection.getMetaData();
        try(ResultSet tables = metaData.getTables(
                null, null, "ls_upgrades", new String[]{"TABLE"})) {
            return tables.next();
        }
    }

    private String getContent(String resource) {
        try {
            InputStream input = loginSecurity.getResource(resource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder builder = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            reader.close();
            return builder.toString();
        } catch(Exception e) {
            throw new RuntimeException("Couldn't read resource content", e);
        }
    }

    private List<String> readMigrations() {
        List<String> migrations = new ArrayList<>();
        try {
            JarFile jarFile = new JarFile(getPluginFile());
            Enumeration<JarEntry> entries = jarFile.entries();
            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if(entry.getName().startsWith("sql/sqlite/") && entry.getName().contains("__")) {
                    migrations.add(entry.getName().substring("sql/sqlite/".length()));
                }
            }
        } catch(IOException e) {
            loginSecurity.getLogger().log(Level.SEVERE, "Failed to scan migration scripts!");
        }

        // Sort the migrations by version
        migrations.sort((o1, o2) -> {
            int i0 = Integer.valueOf(o1.split(Pattern.quote("__"))[0]);
            int i1 = Integer.valueOf(o2.split(Pattern.quote("__"))[0]);
            return Integer.compare(i0, i1);
        });
        return migrations;
    }

    private File getPluginFile() {
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);
            return (File) method.invoke(loginSecurity);
        } catch(Exception e) {
            throw new RuntimeException("Couldn't get context class loader", e);
        }
    }
}
