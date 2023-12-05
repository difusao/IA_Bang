# Bang
Bang IA Neural Networks

## Demonstration
- Windows Desktop Resolution:
1123px x 540px
![Bang](https://github.com/difusao/Bang/blob/master/android/assets/images/BangDesktop.PNG)

- Android Resolution:
2246px x 1080px
![Bang](https://github.com/difusao/Bang/blob/master/android/assets/images/BangAndroid.png)

## Installation
- AndroidStudio 3.4
- JRE 1.8.0_152

## How does it work
- Utilizando algoritmo de Redes Neurais de aprendizado não-supervisionado Perceptron com retropopagação (Backpropagation) de camada de entrada=1, camada oculta=6 e camada de saída=2 correspondente ao Alvo(entrada), Angulo e Força(saída).
- Com população de 20 lançamentos com pesos inicialmente aleatórios que convergem para o alvo a cada lançamento.
- O algoritmo corrige a margem de erro de cada lançamento relação ao alvo até que todos os lançamentos atinjam o alvo.

## Genetic Algorithm
- Após os lançamentos iniciais aleatórios é aplicada a seleção do melhor lançamento da geração atual.
- Os lançamentos que não resultaram em acertos são substituidos pelos melhores lançamentos e clonados com uma taxa de mutação de 5% até a quantidade total de lançamentos da população são aplicados a lista geral de lançamentos.
- O algoritmo corrige a tragetória de cada lançamento até todos os objetos atinjam o alvo.
- Quando todos os objetos atingirem o alvo uma nova posição aleatória do alvo é aplicada e todo processo de aprendizagem é aplicado.

## Implementation
- 

### Be aware of a game bug
- 

## Credits
- [Difusão](https://github.com/difusao)
