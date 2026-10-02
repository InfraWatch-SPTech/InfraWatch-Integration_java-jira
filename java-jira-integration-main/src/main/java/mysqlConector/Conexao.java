package mysqlConector;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

public class Conexao {
    private DataSource conexao;

    public Conexao() {
        DriverManagerDataSource driver = new DriverManagerDataSource();
        driver.setUsername("infra_watch_java_jira");
        driver.setPassword("Urubu100");
        driver.setUrl("jdbc:mysql://localhost:3306/InfraWatch");
        driver.setDriverClassName("com.mysql.cj.jdbc.Driver");

        this.conexao = driver;
    }

    public DataSource getConexao(){
        return this.conexao;
    };
}
