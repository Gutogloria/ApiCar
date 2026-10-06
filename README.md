# ApiCar

App Android nativo, em Kotlin, para cadastrar e gerenciar carros: cada carro tem foto, ano, placa e a localização marcada no mapa. Os dados ficam numa API REST, as fotos no Firebase Storage, e o acesso é feito com login por SMS.

Desenvolvido durante as aulas da pós-graduação em Programação de Dispositivos Móveis (UTFPR) e publicado ao final da disciplina.

## Telas
1. **Login:** entrada com o número de celular e o código recebido por SMS.
2. **Lista de carros:** todos os carros cadastrados na API, com foto, nome e ano.
3. **Novo carro:** formulário com nome, ano, placa, foto pela câmera e local escolhido no mapa.
4. **Detalhes do carro:** informações do carro e local no mapa, com opções de editar e excluir.
   
## Funcionalidades

- **Login por SMS** com Firebase Authentication: o usuário informa o celular, recebe um código de 6 dígitos e entra no app.
- **Lista de carros** carregada da API, com foto, nome e ano de cada um.
- **Cadastro de carro** com nome, ano, placa, foto tirada pela câmera e local escolhido tocando no mapa.
- **Detalhes do carro** com o local mostrado no Google Maps, além de edição e exclusão.
- **Localização do usuário** salva no celular com Room; a última posição é enviada em todas as chamadas à API, em cabeçalhos HTTP, por um interceptor do OkHttp.
- **Tratamento de erros** centralizado nas chamadas de rede (`SafeApiCall`), com mensagens claras quando a API falha.

## Tecnologias

- **Kotlin** e **Coroutines** para as chamadas assíncronas
- **Retrofit**, **OkHttp** e **Gson** para consumir a API REST (GET, POST, PATCH e DELETE)
- **Room** para o banco de dados local
- **Firebase Authentication** (telefone) e **Firebase Storage** (imagens)
- **Google Maps SDK** e **Fused Location Provider** para mapa e localização
- **Picasso** para carregar as imagens
- **View Binding**, **RecyclerView** e **SwipeRefreshLayout** nas telas

## Estrutura

    app/src/main/java/com/example/apicar/
      MainActivity.kt          login por SMS
      HomeActivity.kt          lista de carros e localização do usuário
      NewCarActivity.kt        cadastro com câmera, mapa e upload da foto
      CarDetailActivity.kt     detalhes, edição e exclusão
      adapter/                 adapter da lista (RecyclerView)
      model/                   modelos de dados (Car, CarLocation)
      service/                 Retrofit, interceptor de localização e SafeApiCall
      database/                Room: entidade, DAO e conversores

## Como rodar

1. Clone o repositório e abra no **Android Studio**.
2. Crie um projeto no **Firebase**, ative a autenticação por telefone e o Storage, e coloque o seu `google-services.json` em `app/`.
3. Gere uma chave do **Google Maps SDK for Android** e informe no `AndroidManifest.xml`.
4. Suba a API de carros na porta `3000` da sua máquina. O app acessa `http://10.0.2.2:3000/`, que é o endereço do computador visto de dentro do emulador.
5. Rode o app no emulador (Android 7.0, API 24, ou superior).
