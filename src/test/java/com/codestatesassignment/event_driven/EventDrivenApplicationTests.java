package com.codestatesassignment.event_driven;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

@SpringBootTest
class EventDrivenApplicationTests {

    /** ["Blenders", "Old", "Johnnie"] 와 "[Pride", "Monk", "Walker”] 를 순서대로 하나의 스트림으로 처리되는 로직 검증 */
    @Test
    public void concatWithDelay(){
        Flux<String> names1$ = Flux.just("Blenders", "Old", "Johnnie")
                .delayElements(Duration.ofSeconds(1));
        Flux<String> names2$ = Flux.just("Pride", "Monk", "Walker")
                .delayElements(Duration.ofSeconds(1));
        Flux<String> names$ = Flux.concat(names1$, names2$)
                .log();

        StepVerifier.create(names$)
                .expectSubscription()
                .expectNext("Blenders", "Old", "Johnnie", "Pride", "Monk", "Walker")
                .verifyComplete();
    }

    /** 1~100 까지의 자연수 중 짝수만 출력하는 로직 검증 */
    @Test
    public void printEven(){
        Flux<Integer> flux = Flux.range(1,100)
                .filter(i -> i % 2 == 0)
                .log();

        StepVerifier.create(flux)
                .thenConsumeWhile(i->i % 2 ==0)
                .verifyComplete();
    }

    /** “hello”, “there” 를 순차적으로 publish하여 순서대로 나오는지 검증 */
    @Test
    public void orderPublish(){
        Mono<String> mono1 = Mono.just("hello");
        Mono<String> mono2 = Mono.just("there");

        Flux<String> flux = Flux.concat(mono1,mono2)
                                .publishOn(Schedulers.boundedElastic())
                                .log();

        StepVerifier.create(flux)
                .expectNext("hello","there")
                .verifyComplete();

    }

    /**
     * 아래와 같은 객체가 전달될 때 “JOHN”, “JACK” 등 이름이 대문자로 변환되어 출력되는 로직 검증
     * Person("John", "[john@gmail.com](mailto:john@gmail.com)", "12345678")
     * Person("Jack", "[jack@gmail.com](mailto:jack@gmail.com)", "12345678")
     */
     @Test
     public void mapFlux(){
         class Person {
             private String name;
             private String email;
             private String password;

             public String getName() {
                 return name;
             }

             public Person(String name, String email, String password) {
                 this.name = name;
                 this.email = email;
                 this.password = password;
             }
         }

         Person person1 = new Person("John", "[john@gmail.com](mailto:john@gmail.com)", "12345678");
         Person person2 = new Person("Jack", "[jack@gmail.com](mailto:jack@gmail.com)", "12345678");

         Flux<String> flux = Flux.just(person1, person2)
                 .map(i -> i.getName().toUpperCase())
                 .log();

         StepVerifier.create(flux)
                 .expectNext("JOHN","JACK")
                 .verifyComplete();

         ///git test
      }

    /**
     * 5. ["Blenders", "Old", "Johnnie"] 와 "[Pride", "Monk", "Walker”]를 압축하여 스트림으로 처리 검증
     * 예상되는 스트림 결과값 ["Blenders Pride", "Old Monk", "Johnnie Walker”]
     */
    @Test
    public void zipStream(){
        Flux<String> names1 = Flux.just("Blenders", "Old", "Johnnie");
        Flux<String> names2 = Flux.just("Pride", "Monk", "Walker");

        Flux<String> flux = Flux.zip(names1,names2,(a,b) -> a+" "+b).log();

        StepVerifier.create(flux)
                .expectNext("Blenders Pride", "Old Monk", "Johnnie Walker")
                .verifyComplete();
    }
    /**
     * 6. ["google", "abc", "fb", "stackoverflow”] 의 문자열 중 5자 이상 되는 문자열만 대문자로 비동기로 치환하여 1번 반복하는 스트림으로 처리하는 로직 검증
     * 예상되는 스트림 결과값 ["GOOGLE", "STACKOVERFLOW", "GOOGLE", "STACKOVERFLOW"]
     */

    @Test
    public void searchCondition(){
        Flux<String> words = Flux.just("google", "abc", "fb", "stackoverflow")
                .filter(w->w.length() >= 5)
                .map(w->w.toUpperCase())
                .publishOn(Schedulers.parallel())
                .repeat(1)
                .log();

        StepVerifier.create(words)
                .expectNext("GOOGLE", "STACKOVERFLOW", "GOOGLE", "STACKOVERFLOW")
                .verifyComplete();
    }

}
