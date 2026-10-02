package mysqlConector;

public class ParametroAlerta {
    private Double limite_atencao;
    private Double limite_critico;

    public ParametroAlerta(){

    }

    public ParametroAlerta (Double limite_atencao, Double limite_critico){
        this.limite_atencao = limite_atencao;
        this.limite_critico = limite_critico;


    }

    public Double getLimite_atencao() {
        return limite_atencao;
    }

    public void setLimite_atencao(Double limite_atencao) {
        this.limite_atencao = limite_atencao;
    }

    public Double getLimite_critico() {
        return limite_critico;
    }

    public void setLimite_critico(Double limite_critico) {
        this.limite_critico = limite_critico;
    }

}
