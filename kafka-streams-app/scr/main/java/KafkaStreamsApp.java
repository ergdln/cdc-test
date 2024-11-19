package scr.main.java;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class KafkaStreamsApp {
    public static void main(String[] args) {
        // Configurações do Kafka Streams
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "debezium-streams-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        StreamsBuilder builder = new StreamsBuilder();
        String[] sourceTopics = System.getenv("SOURCE_TOPIC").split(",");
        String targetTopic = System.getenv("TARGET_TOPIC");

        // Stream de usuários (exemplo: verificar novos cadastros)
        KStream<String, String> usersStream = builder.stream(sourceTopics[0]);
        KStream<String, String> filteredUsers = usersStream.filter((key, value) -> {
            // Exemplo de filtro: eventos de novos usuários (operação "c")
            return value.contains("\"operation\":\"c\"");
        });

        // Processa os dados de criação e extrai informações, enviando como JSON
        filteredUsers.mapValues(value -> {
            JsonObject result = new JsonObject();
            try {
                JsonObject json = JsonParser.parseString(value).getAsJsonObject();
                JsonObject after = json.getAsJsonObject("value").getAsJsonObject("after");
                if (after != null) {
                    // Cria o JSON de saída com as informações desejadas
                    result.addProperty("event_type", "Produto criado");
                    result.addProperty("name", after.get("name").getAsString());
                    result.addProperty("price", after.get("price").getAsDouble());
                } else {
                    result.addProperty("error", "Evento inválido");
                }
            } catch (Exception e) {
                result.addProperty("error", "Erro de parsing: " + e.getMessage());
            }
            // Converte o JSON para string antes de enviar ao tópico
            return result.toString();
        }).to(targetTopic);  // Envia eventos filtrados para o tópico de alertas

        // Inicializa e executa o Kafka Streams
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        // Adiciona um hook para fechar a aplicação Kafka Streams corretamente
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
