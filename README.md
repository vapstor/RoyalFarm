<h1 align="center">
  RoyalFarm
</h1>

<p align="center">
  <strong>Repositório para centralizar o Ambiente de Desenvolvimento</strong>
  <p align="center">
    <img src="https://ci.appveyor.com/api/projects/status/g8d58ipi3auqdtrk/branch/master?svg=true" alt="Config. Device Activity Passing." />
<!--      <img src="https://ci.appveyor.com/api/projects/status/216h1g17b8ir009t?svg=true" alt="Config. Device Activity Crashing." /> -->
    <img src="https://img.shields.io/badge/version-11.4.3-blue.svg" alt="Current APP version." />  
  </p>
</p>

## 📋 Briefing

  Aplicativo para efetuar compras na farmácia RoyalFarm (Sem SDK de cartão de crédito)

  Uso de JDBC Mysql para autenticação, recuperação de produtos e inserção de vendas


## 📖 Requirements
```
   implementation fileTree(include: ['*.jar'], dir: 'libs')
    implementation 'androidx.appcompat:appcompat:1.1.0'
    implementation 'com.google.android.material:material:1.2.0-alpha05'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-savedstate:2.3.0-alpha01'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.0.0'
    implementation 'androidx.transition:transition:1.3.1'
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    implementation 'com.squareup.picasso:picasso:2.71828'
    implementation 'androidx.navigation:navigation-fragment:2.2.1'
    implementation 'com.google.code.gson:gson:2.8.6'
    implementation 'androidx.navigation:navigation-ui:2.2.1'
    implementation 'androidx.lifecycle:lifecycle-extensions:2.2.0'

```

## 🚀 ScreensShots
<div style="float: left">
  
  <img src="app/screenshots/screen4.png?raw=true" width="250"/> 
  <img src="app/screenshots/screen5.png?raw=true" width="250"/>
  <img src="app/screenshots/screen9.png?raw=true" width="250"/>
  <img src="app/screenshots/screen10.png?raw=true" width="250"/>
  <img src="app/screenshots/screen8.png?raw=true" width="250"/>
  <img src="app/screenshots/screen2.png?raw=true" width="250"/>
  <img src="app/screenshots/screen3.png?raw=true" width="250"/> 
  <img src="app/screenshots/screen6.png?raw=true" width="250"/> 
  <img src="app/screenshots/screen7.png?raw=true" width="250"/>
</div>

## 👏 Todo (Desenvolvimento)

- [x] Criar repositório no Github
- [x] Criar SplashScreen
- [x] Utilizar Fundamentos do Material Design
- [x] Utilizar JDBC MySQL
- [x] Configurar botões de BottomNavigation:
      - [x] Pesquisa
      - [x] Home
      - [x] Carrinho
- [X] Utilizar MVVM Architeture 

* Desenvolver LoginPage ["LOGIN"]

  - [x] Implementar TextInputsLayouts e TextViews
  - [x] Implementar autenticação no servidor
  - [x] Implementar Webview para novo cadastro
  
* Desenvolver HomePage ["HOME"]

  -  [x] Implementar Bottom Navigation
  -  [x] Implementar Layout do Card de produto
  -  [x] Implementar HorizontalScrollView para card de produtos (3)
  -  [x] Implementar (+) Ver Mais no fim do HorizontalScrollView
  -  [x] Implementar VerticalScrollView na categoria de produto selecionada (ver mais)
  -  [x] Implementar detalhamento ao clicar no Produto
  -  [x] Implementar Diálogo de detalhamento do produto
  -  [x] Configurar Card de produtos Horizontal dinamicos por categorias
  -  [x] Configurar Card de produtos Vertical dinamicos
  -  [x] Configurar Dialogo de detalhamento do produto
  -  [x] Implementar SwipeRefresh
      
* Desenvolver Pesquisar
  - [x] Implementar tela com VerticalScrollView de produtos com Nome/Cod Barra 
  - [] ~Implementar botão para limpar seção de pesquisa~

* Desenvolver CarrinhoPage
  - [x] Implementar Badge no menu de carrinho com quantidade de itens selecionados 
  - [x] Implementar RecyclerView Simples com lista produtos selecionados
  - [x] Implementar Textview com resultado da compra
  - [x] Implementar Botão para FinalizarCompra
  
* Desenvolver FinalizaCompraPage
  - [ ] Implementar EditTexts para recuperação do endereço de entrega 
  - [ ] ~Implementar ImageView para bandeiras aceitas caso Cartão~
  - [x] Implementar CheckBox para necessidade de troco caso opção "Dinheiro": 
    - [x] Implementar Input "Troco para quanto"
  - [x] Implementar RadioButton para Dinheiro ou Cartão
  - [x] Implementar Linha do Tempo de Pedido

* Desenvolver tela de Compra Efetuada
  - [ ] Implementar Diálogo de confirmação da compra
  - [ ] Configurar INSERT via JDBC
  - [ ] Implementar View "Sucesso de Compra"
  
## How to version

Versionamento será dividido entre

- Mudanças significativas de funcionalidade do App (+x.0.0)
- Adição de novas funcionalidades (0.+x.0)
- Ajustes de Bugs (0.0.+x)

#### Exemplo:

> Foram adicionadas 3 novas telas, 5 novas funcionalidades e corrigidos 15 bugs. Logo a versão continuará 1, porém com 8 incrementos de funcionalidades e 15 correções de bugs. Versão final: 1.8.15

#### 👏 Todo (README.MD)

- [x] Implementar ScreensShots no README.MD
- [x] Adicionar Dependências
- [x] Incrementar Todo(Dev)
