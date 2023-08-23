# XML Utils

In some cases, it could be necessary to change JAXB to another version / distribution. In this situation, just exclude:

````xml
<dependency>
            <groupId>io.alapierre.commons</groupId>
            <artifactId>xml-utils</artifactId>
            <version>${common-utils.version}</version>
            <exclusions>
                <exclusion>
                    <artifactId>istack-commons-runtime</artifactId>
                    <groupId>com.sun.istack</groupId>
                </exclusion>
                <exclusion>
                    <groupId>com.sun.xml.bind</groupId>
                    <artifactId>jaxb-impl</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>javax.xml.bind</groupId>
                    <artifactId>jaxb-api</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>com.sun.xml.bind</groupId>
                    <artifactId>jaxb-core</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
````

it was tested with jakarta.xml.bind:jakarta.xml.bind-api:2.3.3
