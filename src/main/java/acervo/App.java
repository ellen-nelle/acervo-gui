package acervo;

import acervo.ui.TelaPrincipal;
import acervo.util.ConexaoDB;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class App extends Application {
	
	@Override
	public void start(Stage stage) {
		if(!ConexaoDB.testarConexao()) {
			Alert alerta = new Alert(Alert.AlertType.ERROR);
			alerta.setTitle("Error de Conexão");
			alerta.setHeaderText("Não foi possivel conectar ao banco de dados");
			alerta.setContentText("Verifique:\n "
					+ "- O MySQL está rodando?\n "
					+ "- A senha no config.properties está correta?\n"
					+ "Execute o arquivo banco.sql no MySQL Workbench.");
			alerta.showAndWait();
			return;
		}
		
		new TelaPrincipal(stage).mostrar();
	}
	 public static void main (String[] args) {
		 launch(args);
	 }

}
