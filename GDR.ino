
/*****************************************************************************************************
------------------------------------------GDR - Guide Dog Robot---------------------------------------
Versão do Software: 1.1
******************************************************************************************************/
  // inclusão de bibliotecas.    
  #include <Servo.h>    // inclui biblioteca de manipulação do servo motor.    
  #include <AFMotor.h>   // inclui biblioteca de manipulação dos motores DCs.  
  #include <SoftwareSerial.h>
    
  //Definindo os pinos  
  #define trigPinF A8 //Pino TRIG do sensor frontal no pino analógico A8
  #define echoPinF A9 //Pino ECHO do sensor frontal no pino analógico A9
  
  #define trigPinS A10 //Pino TRIG do sensor superior no pino analógico A10
  #define echoPinS A11 //Pino ECHO do sensor superior no pino analógico A11

  #define BUZZER A12  // Define o pino do buzzer (Som) no pino ANALÓGICO A12 
 
  #define IR A13  //Sensor INFRA-VERMELHO no pino analógico A13 
  
  SoftwareSerial bluetooth(14, 15);
  
  
  AF_DCMotor motor1(1);    // Define o motor1 ligado ao M1  
  AF_DCMotor motor2(2);    // Define o motor2 ligado ao M2  
 
  int TempoGirar = 1;//esse é o tempo para o robô girar em 45º com uma bateria de 9v.
  int distanciaObstaculoFrontal = 30; //distância para o robô parar e recalcular o melhor caminho
  int distanciaObstaculoSuperior = 50; //distância para o robô parar e recalcular o melhor caminho
  int velocidadeMotores = 255 ; // velocidade que os motores funcionarão na bateria 9v. Para a bateria 9v a velocidade 80 é ideal
  Servo servo_ultra_sonico; // nomeando o servo motor    
  
  //variáveis  para o sensor ultrassonico
  long duracao_frontal;
  long distancia_cm_frontal=0;
  long duracao_superior;
  long distancia_cm_superior=0;
  int minimumRange=5; //tempo de resposta do sensor
  int maximumRange=200;
  
  //variáveis  para o sensor IR
  int status_IR = 1;
    
  // executado na inicialização do Arduino    
  void setup(){    
    Serial.begin(9600); // inicializa a comunicação serial para mostrar dados  
    bluetooth.begin(9600); // inicializa a comunicação bluetooth   
    
    servo_ultra_sonico.attach(10);  // Define o mini servo motor ligado no pino digital 10.    
    
    pinMode(trigPinF, OUTPUT); //define o pino TRIG como saída
    pinMode(echoPinF, INPUT);  //define o pino ECHO como entrada 
    
    pinMode(trigPinS, OUTPUT); //define o pino TRIG como saída
    pinMode(echoPinS, INPUT);  //define o pino ECHO como entrada 
    
    pinMode(IR, INPUT);  //define o pino IR como entrada 
    
    pinMode(BUZZER,OUTPUT);   // Define o pino do buzzer como saída   
    
    motor1.setSpeed(velocidadeMotores);     // Define a velocidade para os motores. A velocidade máxima é 255. 
    motor2.setSpeed(velocidadeMotores);     // Define a velocidade para os motores. A velocidade máxima é 255.
    
    servo_ultra_sonico.write(90);   // O servo do sensor se inicia a 90 graus (meio)    
    rotacao_Parado;  //inica com os motores parados     
    
    digitalWrite(BUZZER, HIGH); // SOM AO LIGAR
    delay(1500);  
    digitalWrite(BUZZER, LOW); // Desliga o som
 
    bluetooth.println("Bluetooth OK!");   
  }    
    
  // Função principal do Arduino    
  void loop(){ 
    pensar(); //inicia a função pensar  
  }    
    
  // Função para chamar outras funções e definir o que o robô fará  
  void pensar(){   
   status_IR = digitalRead(IR); // Ler o sensor infra-vermelho
   reposicionaServoSonar(); //Coloca o servo para olhar a frente    
   int distanciaFrontal = lerSonarFrontal(); // Ler o sensor de distância frontal
   int distanciaSuperior = lerSonarSuperior(); // Ler o sensor de distância superior
   Serial.print("distancia Frontal: "); 
   Serial.println(distanciaFrontal);   // Exibe no serial monitor 
   Serial.print("distancia Superior: "); 
   Serial.println(distanciaSuperior);   // Exibe no serial monitor
  
   if (status_IR == 1){
     Serial.println("Buraco detectado!");   // Exibe no serial monitor   
     rotacao_Parado();  //para o robô 
     pensar(); 
   }  
   
   if ((distanciaFrontal > distanciaObstaculoFrontal) && (distanciaSuperior > distanciaObstaculoSuperior)) {  // Se a distância for maior que distancia dos obstaculos frontal e superior  
     Serial.println("Caminho sem buraco!");   // Exibe no serial monitor
     rotacao_Frente(); //robô anda para frente   
   }else{  
     rotacao_Parado();  //para o robô  
     posicionaCarroMelhorCaminho(); //calcula o melhor caminho    
     pensar();    
   }   
  }  
    
  // Função para ler e calcular a distância do sensor frontal    
  int lerSonarFrontal(){
   
   digitalWrite(trigPinF, LOW); //não envia som (pulso BAIXO de 2 microsegundos para depois enviar pulso ALTO mais limpo) 
   delayMicroseconds(2);
   digitalWrite(trigPinF,HIGH); //envia som 
   delayMicroseconds(10);
   digitalWrite(trigPinF,LOW); //não envia o som e espera o retorno do som enviado
   duracao_frontal = pulseIn(echoPinF,HIGH); //Captura a duração em tempo do retorno do som.
   distancia_cm_frontal = duracao_frontal/56; //Calcula a distância (velocidade do som 340m/s ou 28 microsegundos/cm - o sinal vai e volta, entao dividiremos o pulso por 28 * 2 = 56)
   delay(30);  
   return distancia_cm_frontal;             // Retorna a distância frontal
   }   
    
   // Função para ler e calcular a distância do sensor superior    
   int lerSonarSuperior(){
   
   digitalWrite(trigPinS, LOW); //não envia som (pulso BAIXO de 2 microsegundos para depois enviar pulso ALTO mais limpo) 
   delayMicroseconds(2);
   digitalWrite(trigPinS,HIGH); //envia som 
   delayMicroseconds(10);
   digitalWrite(trigPinS,LOW); //não envia o som e espera o retorno do som enviado
   duracao_superior = pulseIn(echoPinS,HIGH); //Captura a duração em tempo do retorno do som.
   distancia_cm_superior = duracao_superior/56; //Calcula a distância (velocidade do som 340m/s ou 28 microsegundos/cm - o sinal vai e volta, entao dividiremos o pulso por 28 * 2 = 56)
   delay(30);  
   return distancia_cm_superior;             // Retorna a distância frontal
   }
    
  // Função para calcular a distância do centro (FALTA CONFIGURAR SENSOR SUPERIOR!)
  int calcularDistanciaCentro(){    
   servo_ultra_sonico.write(90);    
   delay(20);   
   int leituraDoSonarFrontal = lerSonarFrontal();  // Ler sensor frontal  
   int leituraDoSonarSuperior = lerSonarSuperior();  // Ler sensor superior  
   delay(500);   
   leituraDoSonarFrontal = lerSonarFrontal();
   leituraDoSonarSuperior = lerSonarSuperior();  
   delay(500);   
   Serial.print("Distancia do Centro: "); // Exibe no serial  
   Serial.println(leituraDoSonarFrontal, leituraDoSonarSuperior);   
   return leituraDoSonarFrontal;       // Retorna a distância  
  }    
    
  // Função para calcular a distância da direita (FALTA CONFIGURAR SENSOR SUPERIOR!)
  int calcularDistanciaDireita(){    
   servo_ultra_sonico.write(45);   
   delay(200);  
   int leituraDoSonarFrontal = lerSonarFrontal();  // Ler sensor frontal  
   delay(500);   
   leituraDoSonarFrontal = lerSonarFrontal();
   delay(500);   
   Serial.print("Distancia da Direita: ");  
   Serial.println(leituraDoSonarFrontal);   
   return leituraDoSonarFrontal;       // Retorna a distância     
  }    
    
  // Função para calcular a distância da esquerda (FALTA CONFIGURAR SENSOR SUPERIOR!)   
  int calcularDistanciaEsquerda(){    
   servo_ultra_sonico.write(135);   
   delay(200);  
   int leituraDoSonarFrontal = lerSonarFrontal();  // Ler sensor frontal  
   delay(500);   
   leituraDoSonarFrontal = lerSonarFrontal();
   delay(500);   
   Serial.print("Distancia Esquerda: ");  
   Serial.println(leituraDoSonarFrontal);   
   return leituraDoSonarFrontal;       // Retorna a distância     
  }    
    
  // Função para captar as distâncias lidas e calcular a melhor distância. (FALTA CONFIGURAR SENSOR SUPERIOR!)    
  char calculaMelhorDistancia(){    
   int esquerda = calcularDistanciaEsquerda();    
   int centro = calcularDistanciaCentro();    
   int direita = calcularDistanciaDireita();    
   reposicionaServoSonar();    
   int maiorDistancia = 0;   
   char melhorDistancia = '0';     
     
   if (centro > direita && centro > esquerda){    
     melhorDistancia = 'c';    
     maiorDistancia = centro;    
   }else   
   if (direita > centro && direita > esquerda){    
     melhorDistancia = 'd';    
     maiorDistancia = direita;    
   }else  
   if (esquerda > centro && esquerda > direita){    
     melhorDistancia = 'e';    
     maiorDistancia = esquerda;    
   }    
   if (maiorDistancia <= distanciaObstaculoFrontal) { //distância limite para parar o robô   
     rotacao_Re();    
     posicionaCarroMelhorCaminho();    
   }    
   reposicionaServoSonar();  
   return melhorDistancia;    
  }    
    
  // Função para colocar o carrinho na melhor distância, isto é, girá-lo para a melhor distância    
  void posicionaCarroMelhorCaminho(){    
   char melhorDist = calculaMelhorDistancia();     
   Serial.print("melhor Distancia em cm: ");  
   Serial.println(melhorDist);  
   if (melhorDist == 'c'){    
     pensar();    
   }else if (melhorDist == 'd'){    
     rotacao_Direita();    
   }else if (melhorDist == 'e'){    
     rotacao_Esquerda();     
   }else{    
     rotacao_Re();    
   }    
   reposicionaServoSonar();    
  }    
    
  // Função para deixar o sensor "olho" do robô no centro    
  void reposicionaServoSonar(){    
   servo_ultra_sonico.write(90);   
   delay(200);   
  }    
    
  // Função para fazer o carro parar    
  void rotacao_Parado()    
  {    
   Serial.println(" Motor: Parar ");
   motor1.run(RELEASE); // Motor para  
   motor2.run(RELEASE);  
  }    
    
  // Função para fazer o robô andar para frente    
  void rotacao_Frente()    
  {    
   Serial.println("Motor: Frente "); 
   digitalWrite(BUZZER, HIGH); // Liga o som 
   delay(100);  
   digitalWrite(BUZZER, LOW); // Desliga o som  
   delay(100); 
   motor1.run(FORWARD); // Roda vai para frente  
   motor2.run(FORWARD);   
   delay(50);    
  }    
    
  // Função que faz o robô andar para trás e emite som quando ele dá ré    
  void rotacao_Re()    
  {    
   Serial.println("Motor: ré ");  
   for (int i=0; i <= 3; i++){
      digitalWrite(BUZZER, HIGH); // Liga o som
      delay(100);
      motor1.run(BACKWARD);    // Roda vai para trás  
      motor2.run(BACKWARD);    // Roda vai para trás  
      delay(100);  
      digitalWrite(BUZZER, LOW); // Desliga o som  
      delay(100);
   } 
   rotacao_Parado();    
  }    
    
  // Função que faz o robô virar à direita,     
  void rotacao_Direita()    
  {    
   digitalWrite(BUZZER, HIGH); // Liga o som
   delay(100);
   motor1.run(BACKWARD);    //o robô dá uma ré para não colidir ao girar 
   motor2.run(BACKWARD);      
   delay(50);  
   digitalWrite(BUZZER, LOW); // Desliga o som  
   delay(100);
   Serial.println(" Para a direita ");  
   motor1.run(FORWARD); // Roda vai para frente  
   motor2.run(BACKWARD); // Roda vai para trás   
   delay(TempoGirar);    
  }    
    
  // Função que faz o robô virar à esquerda    
  void rotacao_Esquerda()    
  {    
   digitalWrite(BUZZER, HIGH); // Liga o som
   delay(100);
   motor1.run(BACKWARD);    // // O robô dá uma ré para não colidir ao girar 
   motor2.run(BACKWARD);   
   delay(50);  
   digitalWrite(BUZZER, LOW); // Desliga o som  
   delay(100);
   Serial.println(" Para a esquerda ");  
   motor1.run(BACKWARD); // Roda vai para trás  
   motor2.run(FORWARD); // Roda vai para frente  
   delay(TempoGirar);    
  }
