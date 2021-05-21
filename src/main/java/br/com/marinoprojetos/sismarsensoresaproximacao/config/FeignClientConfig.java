package br.com.marinoprojetos.sismarsensoresaproximacao.config;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import br.com.marinoprojetos.sismarsensoresaproximacao.converters.LocalDateTimeDeserializer;
import br.com.marinoprojetos.sismarsensoresaproximacao.converters.LocalDateTimeSerializer;
import br.com.marinoprojetos.sismarsensoresaproximacao.models.Config;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.ConfigService;
import feign.Client;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Decoder;

@Configuration
public class FeignClientConfig {
	
	@Autowired
	private ConfigService configService;	
	
	@Bean
    public ObjectMapper objectMapperJackson() {
		
        ObjectMapper mapper = new ObjectMapper();           
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        mapper.registerModule(simpleModule);        
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        return mapper;
    }
	
	@Bean
    public Decoder feignDecoder(@Autowired ObjectMapper objectMapper) {				
		HttpMessageConverter<?> jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(jacksonConverter);
        return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
    }
	
	@Bean
	public RequestInterceptor tokenAuthRequestInterceptor() {
		return new RequestInterceptor() {			
			@Override
			public void apply(RequestTemplate template) {
				Config config = configService.getConfig();				
				template.header("Authorization", "Bearer " + (config == null ? "" : config.getToken()));
			}
		};
	}
	
	@Bean
	public Client feignClient(){
	    return new Client.Default(getSSLSocketFactory(), new NoopHostnameVerifier());
	}

	private SSLSocketFactory getSSLSocketFactory() {
	    try {
	        TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
	            @Override
	            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	                return true;
	            }
	        };
	        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
	        return sslContext.getSocketFactory();
	    } catch (Exception exception) {
	    }
	    return null;
	}	
	
}
