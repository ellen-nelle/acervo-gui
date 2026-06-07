package acervo.ui;

import acervo.dao.LivroDAO;
import acervo.model.Livro;
import acervo.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

// =====================================================
//  TelaPrincipal.java — página inicial do sistema
//
//  Mostra os livros disponíveis em cartões visuais.
//  No canto superior direito tem o formulário de login.
// =====================================================
public class TelaPrincipal {

    private Stage stage;
    private LivroDAO livroDAO = new LivroDAO();

    public TelaPrincipal(Stage stage) {
        this.stage = stage;
    }

    public void mostrar() {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #F5F5F5;");
        raiz.setTop(criarCabecalho());
        raiz.setCenter(criarConteudoCentral());

        Scene cena = new Scene(raiz, 1000, 700);
        stage.setTitle("Sistema de Biblioteca");
        stage.setScene(cena);
        stage.show();
    }

    // --------------------------------------------------
    //  CABEÇALHO com título e área de login
    // --------------------------------------------------
    private HBox criarCabecalho() {
        HBox cabecalho = new HBox();
        cabecalho.setStyle("-fx-background-color: #1565C0; -fx-padding: 15px;");
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("📚 Sistema de Biblioteca");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titulo.setTextFill(Color.WHITE);

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        cabecalho.getChildren().addAll(titulo, espaco, criarAreaLogin());
        return cabecalho;
    }

    // --------------------------------------------------
    //  ÁREA DE LOGIN — muda conforme estado do usuário
    // --------------------------------------------------
    private HBox criarAreaLogin() {
        HBox areaLogin = new HBox(10);
        areaLogin.setAlignment(Pos.CENTER_RIGHT);

        if (!SessionManager.isLogado()) {
            TextField campoUsuario = new TextField();
            campoUsuario.setPromptText("Usuário");
            campoUsuario.setPrefWidth(120);

            PasswordField campoSenha = new PasswordField();
            campoSenha.setPromptText("Senha");
            campoSenha.setPrefWidth(100);

            Button btnLogin = new Button("Entrar");
            btnLogin.setStyle(
                "-fx-background-color: #FFC107; -fx-text-fill: #1A1A1A;" +
                "-fx-font-weight: bold; -fx-cursor: hand;"
            );

            Label lblErro = new Label("");
            lblErro.setTextFill(Color.web("#FFCDD2"));
            lblErro.setFont(Font.font(12));

            btnLogin.setOnAction(e -> {
                String usuario = campoUsuario.getText().trim();
                String senha   = campoSenha.getText().trim();
                if (usuario.isEmpty() || senha.isEmpty()) {
                    lblErro.setText("Preencha usuário e senha");
                    return;
                }
                try {
                    var resultado = new acervo.dao.AdminDAO().verificarLogin(usuario, senha);
                    if (resultado.isPresent()) {
                        SessionManager.login(resultado.get());
                        mostrar();
                    } else {
                        lblErro.setText("Usuário ou senha incorretos");
                    }
                } catch (Exception ex) {
                    lblErro.setText("Erro: " + ex.getMessage());
                }
            });

            campoSenha.setOnAction(e -> btnLogin.fire());
            areaLogin.getChildren().addAll(lblErro, campoUsuario, campoSenha, btnLogin);

        } else {
            Label lblBemVindo = new Label("Olá, " + SessionManager.getNomeAdmin());
            lblBemVindo.setTextFill(Color.WHITE);
            lblBemVindo.setFont(Font.font("Arial", FontWeight.BOLD, 13));

            Button btnPainel = new Button("⚙ Painel Admin");
            btnPainel.setStyle(
                "-fx-background-color: #FFC107; -fx-text-fill: #1A1A1A;" +
                "-fx-font-weight: bold; -fx-cursor: hand;"
            );
            btnPainel.setOnAction(e -> new TelaPainel(stage).mostrar());

            Button btnSair = new Button("Sair");
            btnSair.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white;" +
                "-fx-border-color: white; -fx-border-radius: 4px; -fx-cursor: hand;"
            );
            btnSair.setOnAction(e -> { SessionManager.logout(); mostrar(); });

            areaLogin.getChildren().addAll(lblBemVindo, btnPainel, btnSair);
        }
        return areaLogin;
    }

    // --------------------------------------------------
    //  CONTEÚDO CENTRAL — pesquisa + grade de cartões
    // --------------------------------------------------
    private VBox criarConteudoCentral() {
        VBox conteudo = new VBox(20);
        conteudo.setPadding(new Insets(25));

        Label lblTitulo = new Label("Livros Disponíveis");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblTitulo.setTextFill(Color.web("#1565C0"));

        // ── Barra de pesquisa ─────────────────────────
        HBox barraPesquisa = new HBox(10);
        barraPesquisa.setAlignment(Pos.CENTER_LEFT);

        TextField campoPesquisa = new TextField();
        campoPesquisa.setPromptText("🔍  Pesquisar por título, autor ou gênero...");
        campoPesquisa.setPrefWidth(380);
        campoPesquisa.setPrefHeight(38);
        campoPesquisa.setStyle("-fx-font-size: 14px; -fx-padding: 5px 10px;");

        Button btnBuscar = new Button("Buscar");
        estilizarBotaoAzul(btnBuscar);
        btnBuscar.setPrefHeight(38);

        Button btnTodos = new Button("Ver Todos");
        btnTodos.setStyle(
            "-fx-background-color: transparent; -fx-border-color: #1565C0;" +
            "-fx-border-radius: 5px; -fx-text-fill: #1565C0;" +
            "-fx-cursor: hand; -fx-padding: 5px 14px;"
        );
        btnTodos.setPrefHeight(38);

        barraPesquisa.getChildren().addAll(campoPesquisa, btnBuscar, btnTodos);

        // ── Grade de livros ───────────────────────────
        FlowPane grade = new FlowPane();
        grade.setHgap(18);
        grade.setVgap(18);
        grade.setPadding(new Insets(10, 0, 0, 0));

        carregarLivros(grade, null);

        btnBuscar.setOnAction(e -> {
            String termo = campoPesquisa.getText().trim();
            carregarLivros(grade, termo.isEmpty() ? null : termo);
        });
        campoPesquisa.setOnAction(e -> btnBuscar.fire());
        btnTodos.setOnAction(e -> { campoPesquisa.clear(); carregarTodos(grade); });

        ScrollPane scroll = new ScrollPane(grade);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        conteudo.getChildren().addAll(lblTitulo, barraPesquisa, scroll);
        return conteudo;
    }

    // --------------------------------------------------
    //  CARREGAR LIVROS
    // --------------------------------------------------
    private void carregarLivros(FlowPane grade, String termo) {
        grade.getChildren().clear();
        try {
            List<Livro> livros = (termo != null)
                ? livroDAO.buscar(termo)
                : livroDAO.listarDisponiveis();

            if (livros.isEmpty()) {
                Label vazio = new Label("Nenhum livro encontrado.");
                vazio.setFont(Font.font(15));
                vazio.setTextFill(Color.GRAY);
                grade.getChildren().add(vazio);
            } else {
                for (Livro l : livros) grade.getChildren().add(criarCartao(l));
            }
        } catch (Exception ex) {
            Label erro = new Label("Erro ao carregar livros: " + ex.getMessage());
            erro.setTextFill(Color.RED);
            grade.getChildren().add(erro);
        }
    }

    private void carregarTodos(FlowPane grade) {
        grade.getChildren().clear();
        try {
            for (Livro l : livroDAO.listarTodos()) grade.getChildren().add(criarCartao(l));
        } catch (Exception ex) {
            grade.getChildren().add(new Label("Erro: " + ex.getMessage()));
        }
    }

    // --------------------------------------------------
    //  CARTÃO DE LIVRO — exibe todos os campos novos
    // --------------------------------------------------
    private VBox criarCartao(Livro livro) {
        VBox cartao = new VBox(6);
        cartao.setPrefWidth(210);
        cartao.setPadding(new Insets(15));
        cartao.setStyle(
            "-fx-background-color: white; -fx-border-color: #E0E0E0;" +
            "-fx-border-radius: 8px; -fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        );

        // Ícone
        Label icone = new Label("📖");
        icone.setFont(Font.font(36));
        icone.setMaxWidth(Double.MAX_VALUE);
        icone.setAlignment(Pos.CENTER);

        // Título
        Label lblTitulo = new Label(livro.getTitulo());
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblTitulo.setWrapText(true);
        lblTitulo.setMaxWidth(190);

        // Autor
        Label lblAutor = new Label("✍ " + livro.getAutor());
        lblAutor.setFont(Font.font(11));
        lblAutor.setTextFill(Color.GRAY);
        lblAutor.setWrapText(true);

        // Gênero
        Label lblGenero = new Label("📂 " + (livro.getGenero() != null ? livro.getGenero() : "—"));
        lblGenero.setFont(Font.font(11));
        lblGenero.setTextFill(Color.web("#555555"));

        // Ano
        Label lblAno = new Label("📅 " + (livro.getAno() > 0 ? livro.getAno() : "—"));
        lblAno.setFont(Font.font(11));
        lblAno.setTextFill(Color.LIGHTGRAY);

        // Faixa etária
        Label lblFaixa = new Label("👤 " + (livro.getFaixaEtaria() != null ? livro.getFaixaEtaria() : "—"));
        lblFaixa.setFont(Font.font(11));
        lblFaixa.setTextFill(Color.web("#777777"));

        // Status / quantidade
        String statusTexto;
        String statusCor;
        if (livro.isDisponivel()) {
            statusTexto = "✓ " + livro.getQuantidade() + " exemplar(es) disponível(is)";
            statusCor   = "#2E7D32";
        } else {
            statusTexto = "✗ Indisponível";
            statusCor   = "#C62828";
        }
        Label lblStatus = new Label(statusTexto);
        lblStatus.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lblStatus.setTextFill(Color.web(statusCor));
        lblStatus.setWrapText(true);

        cartao.getChildren().addAll(icone, lblTitulo, lblAutor, lblGenero, lblAno, lblFaixa, lblStatus);

        // Efeito hover
        cartao.setOnMouseEntered(e -> cartao.setStyle(
            "-fx-background-color: #E3F2FD; -fx-border-color: #1565C0;" +
            "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);"
        ));
        cartao.setOnMouseExited(e -> cartao.setStyle(
            "-fx-background-color: white; -fx-border-color: #E0E0E0;" +
            "-fx-border-radius: 8px; -fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        ));

        return cartao;
    }

    private void estilizarBotaoAzul(Button btn) {
        btn.setStyle(
            "-fx-background-color: #1565C0; -fx-text-fill: white;" +
            "-fx-font-weight: bold; -fx-border-radius: 5px;" +
            "-fx-background-radius: 5px; -fx-cursor: hand; -fx-padding: 5px 16px;"
        );
    }
}