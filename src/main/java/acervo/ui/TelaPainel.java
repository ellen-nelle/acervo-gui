package acervo.ui;

import acervo.dao.EmprestimoDAO;
import acervo.dao.LivroDAO;
import acervo.model.Emprestimo;
import acervo.model.Livro;
import acervo.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

// =====================================================
//  TelaPainel.java — painel do administrador
//
//  Organizado em 3 abas:
//  → Aba 1: Livros (cadastrar, editar, remover)
//  → Aba 2: Empréstimos (registrar empréstimo e devolução)
//  → Aba 3: Histórico completo
// =====================================================
public class TelaPainel {

    private Stage         stage;
    private LivroDAO      livroDAO      = new LivroDAO();
    private EmprestimoDAO emprestimoDAO = new EmprestimoDAO();

    public TelaPainel(Stage stage) {
        this.stage = stage;
    }

    public void mostrar() {
        BorderPane raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #F5F5F5;");
        raiz.setTop(criarCabecalho());

        TabPane abas = new TabPane();
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        abas.getTabs().addAll(
            new Tab("📚  Livros",      criarAbaLivros()),
            new Tab("🤝  Empréstimos", criarAbaEmprestimos()),
            new Tab("📋  Histórico",   criarAbaHistorico())
        );
        raiz.setCenter(abas);

        stage.setScene(new Scene(raiz, 1100, 720));
    }

    // --------------------------------------------------
    //  CABEÇALHO
    // --------------------------------------------------
    private HBox criarCabecalho() {
        HBox cab = new HBox();
        cab.setStyle("-fx-background-color: #0D47A1; -fx-padding: 15px;");
        cab.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("⚙ Painel Administrativo");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titulo.setTextFill(Color.WHITE);

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        Label lblAdmin = new Label("Logado: " + SessionManager.getNomeAdmin());
        lblAdmin.setTextFill(Color.web("#90CAF9"));
        lblAdmin.setFont(Font.font(13));

        Button btnVoltar = new Button("← Página Inicial");
        btnVoltar.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: white;" +
            "-fx-border-color: white; -fx-border-radius: 4px;" +
            "-fx-cursor: hand; -fx-padding: 5px 12px;"
        );
        btnVoltar.setOnAction(e -> new TelaPrincipal(stage).mostrar());

        cab.getChildren().addAll(titulo, espaco, lblAdmin, new Label("   "), btnVoltar);
        return cab;
    }

    // =====================================================
    //  ABA 1 — LIVROS
    // =====================================================
    private VBox criarAbaLivros() {
        VBox aba = new VBox(15);
        aba.setPadding(new Insets(20));

        // ── Tabela de livros ──────────────────────────
        TableView<Livro> tabela = new TableView<>();
        ObservableList<Livro> dados = FXCollections.observableArrayList();
        tabela.setItems(dados);
        tabela.setPrefHeight(340);

        // Cada coluna exibe um campo do objeto Livro
        // PropertyValueFactory("titulo") chama livro.getTitulo() automaticamente
        TableColumn<Livro, String>  colTitulo = colStr ("Título",       "titulo",      200);
        TableColumn<Livro, String>  colAutor  = colStr ("Autor",        "autor",       150);
        TableColumn<Livro, String>  colIsbn   = colStr ("ISBN",         "isbn",        120);
        TableColumn<Livro, String>  colGenero = colStr ("Gênero",       "genero",      100);
        TableColumn<Livro, Integer> colAno    = colInt ("Ano",          "ano",          60);
        TableColumn<Livro, String>  colFaixa  = colStr ("Faixa Etária", "faixaEtaria",  90);
        TableColumn<Livro, Integer> colQtd    = colInt ("Qtd.",         "quantidade",   55);

        // Coluna de status: calculada a partir de isDisponivel()
        TableColumn<Livro, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().isDisponivel() ? "✓ Disponível" : "✗ Emprestado"
            )
        );
        colStatus.setPrefWidth(100);

        tabela.getColumns().addAll(
            colTitulo, colAutor, colIsbn, colGenero, colAno, colFaixa, colQtd, colStatus
        );

        // ── Botões de ação ────────────────────────────
        Button btnNovo      = new Button("＋ Cadastrar Novo Livro");
        Button btnEditar    = new Button("✏ Editar Selecionado");
        Button btnRemover   = new Button("🗑 Remover");
        Button btnAtualizar = new Button("↻ Atualizar");

        estilizar(btnNovo,      "#1565C0");
        estilizar(btnEditar,    "#1565C0");
        estilizar(btnRemover,   "#C62828");
        estilizar(btnAtualizar, "#388E3C");

        HBox botoes = new HBox(10, btnNovo, btnEditar, btnRemover, btnAtualizar);

        // Carrega os livros ao abrir a aba
        recarregarLivros(dados);

        btnAtualizar.setOnAction(e -> recarregarLivros(dados));

        btnNovo.setOnAction(e ->
            new TelaFormLivro(stage, null, () -> recarregarLivros(dados)).mostrar()
        );

        btnEditar.setOnAction(e -> {
            Livro sel = tabela.getSelectionModel().getSelectedItem();
            if (sel == null) { alerta("Atenção", "Selecione um livro para editar."); return; }
            new TelaFormLivro(stage, sel, () -> recarregarLivros(dados)).mostrar();
        });

        btnRemover.setOnAction(e -> {
            Livro sel = tabela.getSelectionModel().getSelectedItem();
            if (sel == null) { alerta("Atenção", "Selecione um livro para remover."); return; }
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Remover \"" + sel.getTitulo() + "\"?", ButtonType.YES, ButtonType.NO);
            conf.setTitle("Confirmar Remoção");
            conf.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    try {
                        livroDAO.remover(sel.getId());
                        recarregarLivros(dados);
                        sucesso("Livro removido com sucesso!");
                    } catch (Exception ex) { alerta("Erro", ex.getMessage()); }
                }
            });
        });

        aba.getChildren().addAll(tabela, botoes);
        return aba;
    }

    // =====================================================
    //  ABA 2 — EMPRÉSTIMOS
    //  Formulário com os campos do leitor:
    //  → ocupação (Professor ou Aluno)
    //  → nome
    //  → ano escolar e turma (só para alunos)
    //  → prazo em dias
    // =====================================================
    private VBox criarAbaEmprestimos() {
        VBox aba = new VBox(15);
        aba.setPadding(new Insets(20));

        Label lblTitulo = new Label("Registrar Novo Empréstimo");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblTitulo.setTextFill(Color.web("#0D47A1"));

        // ── Formulário ────────────────────────────────
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(15));
        form.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #E0E0E0;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );

        // ComboBox do livro
        form.add(new Label("Livro:"), 0, 0);
        ComboBox<Livro> comboLivro = new ComboBox<>();
        comboLivro.setPrefWidth(300);
        comboLivro.setPromptText("Selecione um livro disponível...");
        form.add(comboLivro, 1, 0, 2, 1); // ocupa 2 colunas

        // Ocupação — Professor ou Aluno
        form.add(new Label("Ocupação:"), 0, 1);
        ComboBox<String> comboOcupacao = new ComboBox<>();
        comboOcupacao.getItems().addAll("Aluno", "Professor", "Funcionário", "Outro");
        comboOcupacao.setPromptText("Selecione...");
        comboOcupacao.setPrefWidth(160);
        form.add(comboOcupacao, 1, 1);

        // Nome do leitor
        form.add(new Label("Nome do leitor:"), 0, 2);
        TextField fNome = campo("Nome completo");
        fNome.setPrefWidth(300);
        form.add(fNome, 1, 2, 2, 1);

        // Ano escolar e turma — ficam na mesma linha
        form.add(new Label("Ano escolar:"), 0, 3);
        TextField fAlunoAno = campo("Ex: 1, 2, 3...");
        fAlunoAno.setPrefWidth(80);
        form.add(fAlunoAno, 1, 3);

        form.add(new Label("Turma:"), 2, 3);
        TextField fAlunoTurma = campo("Ex: A, B, C...");
        fAlunoTurma.setPrefWidth(80);
        form.add(fAlunoTurma, 3, 3);

        // Nota sobre ano/turma
        Label lblNota = new Label("* Ano e turma só para alunos");
        lblNota.setFont(Font.font(10));
        lblNota.setTextFill(Color.GRAY);
        form.add(lblNota, 1, 4, 3, 1);

        // Prazo
        form.add(new Label("Prazo (dias):"), 0, 5);
        TextField fDias = new TextField("14");
        fDias.setPrefWidth(80);
        form.add(fDias, 1, 5);

        // Botão registrar
        Button btnEmprestar = new Button("✓ Registrar Empréstimo");
        estilizar(btnEmprestar, "#388E3C");
        btnEmprestar.setPrefHeight(38);
        btnEmprestar.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        form.add(btnEmprestar, 1, 6, 2, 1);

        // ── Tabela de empréstimos em aberto ───────────
        Label lblAbertos = new Label("Empréstimos em Aberto");
        lblAbertos.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        lblAbertos.setTextFill(Color.web("#0D47A1"));

        TableView<Emprestimo> tabelaAbertos = new TableView<>();
        ObservableList<Emprestimo> dadosAbertos = FXCollections.observableArrayList();
        tabelaAbertos.setItems(dadosAbertos);
        tabelaAbertos.setPrefHeight(200);
        configurarTabelaEmprestimos(tabelaAbertos);

        Button btnDevolver   = new Button("↩ Registrar Devolução");
        Button btnAtualizar  = new Button("↻ Atualizar");
        estilizar(btnDevolver,  "#E65100");
        estilizar(btnAtualizar, "#1565C0");

        // Carrega dados iniciais
        carregarDisponiveis(comboLivro);
        recarregarEmAberto(dadosAbertos);

        // ── Evento: registrar empréstimo ──────────────
        btnEmprestar.setOnAction(e -> {
            try {
                Livro livroSel = comboLivro.getValue();
                if (livroSel == null)
                    throw new Exception("Selecione um livro.");
                if (comboOcupacao.getValue() == null)
                    throw new Exception("Selecione a ocupação do leitor.");
                if (fNome.getText().isBlank())
                    throw new Exception("Informe o nome do leitor.");

                // Ano e turma são obrigatórios só para alunos
                int  alunoAno   = 0;
                char alunoTurma = ' ';
                if (comboOcupacao.getValue().equals("Aluno")) {
                    if (fAlunoAno.getText().isBlank())
                        throw new Exception("Informe o ano escolar do aluno.");
                    if (fAlunoTurma.getText().isBlank())
                        throw new Exception("Informe a turma do aluno.");
                    alunoAno   = Integer.parseInt(fAlunoAno.getText().trim());
                    alunoTurma = fAlunoTurma.getText().trim().toUpperCase().charAt(0);
                }

                int dias = fDias.getText().isBlank() ? 14
                         : Integer.parseInt(fDias.getText().trim());

                // Cria o objeto Emprestimo
                Emprestimo emp = new Emprestimo(
                    0,                              // id — banco gera automaticamente
                    livroSel.getId(),               // livro_id
                    comboOcupacao.getValue(),       // ocupacao_leitor
                    fNome.getText().trim(),         // nome_leitor
                    alunoAno,                       // aluno_ano
                    alunoTurma,                     // aluno_turma
                    java.time.LocalDate.now(),      // data_emprestimo = hoje
                    java.time.LocalDate.now().plusDays(dias), // data_prevista
                    null                            // data_devolucao = null (em aberto)
                );
                emprestimoDAO.inserir(emp);

                // Se não há mais exemplares disponíveis, marca como indisponível
                int emAberto = emprestimoDAO.contarEmAberto(livroSel.getId());
                if (emAberto >= livroSel.getQuantidade())
                    livroDAO.atualizarDisponibilidade(livroSel.getId(), false);

                // Limpa o formulário
                comboLivro.setValue(null);
                comboOcupacao.setValue(null);
                fNome.clear();
                fAlunoAno.clear();
                fAlunoTurma.clear();
                fDias.setText("14");

                carregarDisponiveis(comboLivro);
                recarregarEmAberto(dadosAbertos);
                sucesso("Empréstimo registrado! Prazo: " + dias + " dia(s).");

            } catch (NumberFormatException ex) {
                alerta("Erro", "Ano escolar e prazo devem ser números.");
            } catch (Exception ex) {
                alerta("Erro", ex.getMessage());
            }
        });

        // ── Evento: registrar devolução ───────────────
        btnDevolver.setOnAction(e -> {
            Emprestimo sel = tabelaAbertos.getSelectionModel().getSelectedItem();
            if (sel == null) { alerta("Atenção", "Selecione um empréstimo para devolver."); return; }
            try {
                emprestimoDAO.registrarDevolucao(sel.getId());
                livroDAO.atualizarDisponibilidade(sel.getLivroId(), true);
                carregarDisponiveis(comboLivro);
                recarregarEmAberto(dadosAbertos);
                sucesso("Devolução registrada com sucesso!");
            } catch (Exception ex) { alerta("Erro", ex.getMessage()); }
        });

        btnAtualizar.setOnAction(e -> {
            carregarDisponiveis(comboLivro);
            recarregarEmAberto(dadosAbertos);
        });

        aba.getChildren().addAll(
            lblTitulo, form,
            new Separator(),
            lblAbertos, tabelaAbertos,
            new HBox(10, btnDevolver, btnAtualizar)
        );
        return aba;
    }

    // =====================================================
    //  ABA 3 — HISTÓRICO COMPLETO
    // =====================================================
    private VBox criarAbaHistorico() {
        VBox aba = new VBox(15);
        aba.setPadding(new Insets(20));

        Label lblTitulo = new Label("Histórico de Empréstimos");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblTitulo.setTextFill(Color.web("#0D47A1"));

        TableView<Emprestimo> tabela = new TableView<>();
        ObservableList<Emprestimo> dados = FXCollections.observableArrayList();
        tabela.setItems(dados);
        VBox.setVgrow(tabela, Priority.ALWAYS);
        configurarTabelaEmprestimos(tabela);

        // Coluna de situação — calculada a partir dos métodos do model
        TableColumn<Emprestimo, String> colSit = new TableColumn<>("Situação");
        colSit.setCellValueFactory(c -> {
            Emprestimo em = c.getValue();
            String s = em.isDevolvido()  ? "✓ Devolvido"
                     : em.isAtrasado()   ? "⚠ Atrasado"
                     : "⏳ Em aberto";
            return new SimpleStringProperty(s);
        });
        colSit.setPrefWidth(110);
        tabela.getColumns().add(colSit);

        Button btnAtualizar = new Button("↻ Atualizar");
        estilizar(btnAtualizar, "#1565C0");

        recarregarHistorico(dados);
        btnAtualizar.setOnAction(e -> recarregarHistorico(dados));

        aba.getChildren().addAll(lblTitulo, tabela, btnAtualizar);
        return aba;
    }

    // --------------------------------------------------
    //  COLUNAS DA TABELA DE EMPRÉSTIMOS
    //  Usa os campos do Emprestimo atualizado:
    //  tituloLivro, nomeLeitor, ocupacaoLeitor,
    //  alunoAno, alunoTurma, dataEmprestimo, dataPrevista
    // --------------------------------------------------
    private void configurarTabelaEmprestimos(TableView<Emprestimo> tabela) {

        TableColumn<Emprestimo, String> colLivro = colStr("Livro", "tituloLivro", 180);

        TableColumn<Emprestimo, String> colLeitor = colStr("Leitor", "nomeLeitor", 140);

        TableColumn<Emprestimo, String> colOcupacao = colStr("Ocupação", "ocupacaoLeitor", 100);

        // Ano escolar — número inteiro
        TableColumn<Emprestimo, Integer> colAno = colInt("Ano", "alunoAno", 50);

        // Turma — char, precisa de conversão para String
        TableColumn<Emprestimo, String> colTurma = new TableColumn<>("Turma");
        colTurma.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getAlunoTurma() == '\0' ? "—"
                : String.valueOf(c.getValue().getAlunoTurma())
            )
        );
        colTurma.setPrefWidth(55);

        // Datas — convertidas de LocalDate para String
        TableColumn<Emprestimo, String> colRetirada = new TableColumn<>("Retirada");
        colRetirada.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getDataEmpretismo().toString())
        );
        colRetirada.setPrefWidth(90);

        TableColumn<Emprestimo, String> colPrazo = new TableColumn<>("Prazo");
        colPrazo.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getDataPrevista().toString())
        );
        colPrazo.setPrefWidth(90);

        tabela.getColumns().addAll(
            colLivro, colLeitor, colOcupacao, colAno, colTurma, colRetirada, colPrazo
        );
    }

    // --------------------------------------------------
    //  HELPERS DE CARREGAMENTO DE DADOS
    // --------------------------------------------------
    private void recarregarLivros(ObservableList<Livro> dados) {
        try { dados.setAll(livroDAO.listarTodos()); }
        catch (Exception e) { alerta("Erro", e.getMessage()); }
    }

    private void carregarDisponiveis(ComboBox<Livro> combo) {
        try {
            combo.setItems(FXCollections.observableArrayList(
                livroDAO.listarDisponiveis()
            ));
        } catch (Exception e) { alerta("Erro", e.getMessage()); }
    }

    private void recarregarEmAberto(ObservableList<Emprestimo> dados) {
        try { dados.setAll(emprestimoDAO.listarEmAberto()); }
        catch (Exception e) { alerta("Erro", e.getMessage()); }
    }

    private void recarregarHistorico(ObservableList<Emprestimo> dados) {
        try { dados.setAll(emprestimoDAO.listarTodos()); }
        catch (Exception e) { alerta("Erro", e.getMessage()); }
    }

    // --------------------------------------------------
    //  HELPERS DE UI
    // --------------------------------------------------

    // Cria coluna de String com PropertyValueFactory
    @SuppressWarnings("unchecked")
    private <T> TableColumn<T, String> colStr(String nome, String prop, double larg) {
        TableColumn<T, String> c = new TableColumn<>(nome);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(larg);
        return c;
    }

    // Cria coluna de Integer com PropertyValueFactory
    @SuppressWarnings("unchecked")
    private <T> TableColumn<T, Integer> colInt(String nome, String prop, double larg) {
        TableColumn<T, Integer> c = new TableColumn<>(nome);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(larg);
        return c;
    }

    private TextField campo(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        return tf;
    }

    private void estilizar(Button btn, String cor) {
        btn.setStyle(
            "-fx-background-color: " + cor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5px 14px;"
        );
    }

    private void alerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private void sucesso(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Sucesso"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}