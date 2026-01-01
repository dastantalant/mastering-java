package com.mastering.lox.lox;

import com.mastering.lox.Scanner;
import com.mastering.lox.Token;
import com.mastering.lox.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ScannerTest {

    @Test
    @DisplayName("Can parse something")
    public void canParseSomething() {
        String source = "({})";
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        assertAll("Parses correct tokens",
                () -> assertEquals(TokenType.LEFT_PAREN, tokens.getFirst().type),
                () -> assertEquals(TokenType.LEFT_BRACE, tokens.get(1).type),
                () -> assertEquals(TokenType.RIGHT_BRACE, tokens.get(2).type),
                () -> assertEquals(TokenType.RIGHT_PAREN, tokens.get(3).type)
        );
    }

    @Test
    @DisplayName("Can parse integer as number")
    public void canParseNumber() {
        String source = "(123)";
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();

        assertThat(tokens.size()).isEqualTo(4);
    }

}