package acervo.ui;

import acervo.dao.LivroDAO;
import acervo.model.Livro;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

// =====================================================
//  TelaFormLivro.java — cadastro e edição de livros
//
//  Abre como janela secundária (modal), bloqueando
//  a janela principal enquanto estiver aberta.
//
//  Funciona para dois casos:
//  → livro == null  : cadastrar livro novo
//  → livro != null  : editar livro existente
//
//  O "callback" é executado ao salvar para
//  atualizar a tabela na TelaPainel automaticamente.
// =====================================================
public class TelaFormLivro {

    private Stage    stage;
    private Livro    livro;    // null = novo, não-null = editando
    private Runnable callback; // ação após salvar (ex: recarregar tabela)
    private LivroDAO livroDAO = new LivroDAO();

    public TelaFormLivro(Stage owner, Livro livro, Runnable callback) {
        this.livro    = livro;
        this.callback = callback;

        // Cria uma janela filha da janela principal
        this.stage = new Stage();
        // Modality.WINDOW_MODAL: bloqueia a janela pai
        this.stage.initModality(Modality.WINDOW_MODAL);
        this.stage.initOwner(owner);
    }

    public void mostrar() {
        boolean editando = (livro != null);
        stage.setTitle(editando ? "Editar Livro" : "Cadastrar Novo Livro");

        // ── LAYOUT PRINCIPAL ──────────────────────────
        VBox layout = new VBox(14);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: #FAFAFA;");
        layout.setPrefWidth(450);

        // Título do formulário
        Label lblTitulo = new Label(editando ? "✏ Editar Livro" : "＋ Cadastrar Livro");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(Color.web("#0D47A1"));

        // ── CAMPOS ────────────────────────────────────
        // Se estiver editando, pré-preenche com os dados atuais.
        // Se for novo, os campos ficam vazios.

        TextField fTitulo = criarCampo(
            "Título *",
            editando ? livro.getTitulo() : ""
        );

        TextField fAutor = criarCampo(
            "Autor *",
            editando ? livro.getAutor() : ""
        );

        TextField fIsbn = criarCampo(
            "ISBN",
            editando && livro.getIsbn() != null ? livro.getIsbn() : ""
        );

        TextField fGenero = criarCampo(
            "Gênero  (ex: Romance, Ficção, Didático...)",
            editando && livro.getGenero() != null ? livro.getGenero() : ""
        );

        TextField fAno = criarCampo(
            "Ano de Publicação",
            editando && livro.getAno() > 0 ? String.valueOf(livro.getAno()) : ""
        );

        TextField fFaixaEtaria = criarCampo(
            "Faixa Etária  (ex: Infantil, Juvenil, Adulto...)",
            editando && livro.getFaixaEtaria() != null ? livro.getFaixaEtaria() : ""
        );

        TextField fQuantidade = criarCampo(
            "Quantidade de Exemplares *",
            editando ? String.valueOf(livro.getQuantidade()) : "1"
        );

        // Legenda dos campos obrigatórios
        Label lblObrig = new Label("* Campos obrigatórios");
        lblObrig.setFont(Font.font(11));
        lblObrig.setTextFill(Color.GRAY);

        // ── BOTÕES ────────────────────────────────────
        HBox botoes = new HBox(12);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        botoes.setPadding(new Insets(10, 0, 0, 0));

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle(
            "-fx-background-color: #EEEEEE;" +
            "-fx-text-fill: #333333;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8px 18px;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;"
        );
        btnCancelar.setOnAction(e -> stage.close());

        Button btnSalvar = new Button(editando ? "Salvar Alterações" : "Cadastrar");
        btnSalvar.setStyle(
            "-fx-background-color: #1565C0;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8px 18px;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;"
        );

        // ── EVENTO DO BOTÃO SALVAR ────────────────────
        btnSalvar.setOnAction(e -> {
            try {
                // Validações obrigatórias
                if (fTitulo.getText().isBlank())
                    throw new Exception("O título é obrigatório.");
                if (fAutor.getText().isBlank())
                    throw new Exception("O autor é obrigatório.");
                if (fQuantidade.getText().isBlank())
                    throw new Exception("A quantidade é obrigatória.");

                // Conversão de campos numéricos
                int ano = 0;
                if (!fAno.getText().isBlank())
                    ano = Integer.parseInt(fAno.getText().trim());

                int qtd = Integer.parseInt(fQuantidade.getText().trim());
                if (qtd < 1)
                    throw new Exception("A quantidade deve ser pelo menos 1.");

                if (editando) {
                    // ── MODO EDIÇÃO: atualiza o livro existente ──
                    livro.setTitulo     (fTitulo.getText().trim());
                    livro.setAutor      (fAutor.getText().trim());
                    livro.setIsbn       (fIsbn.getText().trim());
                    livro.setGenero     (fGenero.getText().trim());
                    livro.setAno        (ano);
                    livro.setFaixaEtaria(fFaixaEtaria.getText().trim());
                    livro.setQuantidade (qtd);
                    livroDAO.atualizar(livro);

                } else {
                    // ── MODO CADASTRO: cria livro novo ───────────
                    Livro novo = new Livro(
                        fIsbn.getText().trim(),
                        fTitulo.getText().trim(),
                        fAutor.getText().trim(),
                        fGenero.getText().trim(),
                        ano,
                        fFaixaEtaria.getText().trim(),
                        qtd
                    );
                    livroDAO.inserir(novo);
                }

                // Executa o callback (recarrega a tabela no painel)
                if (callback != null) callback.run();

                // Confirmação e fecha a janela
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Sucesso");
                ok.setHeaderText(null);
                ok.setContentText(editando
                    ? "Livro atualizado com sucesso!"
                    : "Livro cadastrado com sucesso!");
                ok.showAndWait();
                stage.close();

            } catch (NumberFormatException ex) {
                mostrarErro("Ano e quantidade devem ser números inteiros.");
            } catch (Exception ex) {
                mostrarErro(ex.getMessage());
            }
        });

        botoes.getChildren().addAll(btnCancelar, btnSalvar);

        layout.getChildren().addAll(
            lblTitulo,
            fTitulo,
            fAutor,
            fIsbn,
            fGenero,
            fAno,
            fFaixaEtaria,
            fQuantidade,
            lblObrig,
            botoes
        );

        Scene cena = new Scene(layout);
        stage.setScene(cena);
        stage.setResizable(false);
        stage.showAndWait();
    }

    // --------------------------------------------------
    //  HELPER — cria campo de texto com placeholder
    // --------------------------------------------------
    private TextField criarCampo(String placeholder, String valorInicial) {
        TextField campo = new TextField(valorInicial);
        campo.setPromptText(placeholder);
        campo.setPrefHeight(36);
        campo.setStyle("-fx-font-size: 13px; -fx-padding: 5px 10px;");
        return campo;
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}