package org.assertj.core.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * AssertJ 이슈 #3354의 버그를 다양한 컨테이너 타입에서 재현하기 위한 테스트 클래스입니다.
 * <p>
 * 이 버그는 재귀적 비교 시, 컨테이너(Iterable, Array, Optional, AtomicReference) 내 요소의
 * 존재하지 않는 필드를 비교 대상으로 지정해도 예외(Exception)가 발생하지 않는 문제입니다.
 * 올바른 동작이라면 필드를 찾을 수 없다는 예외가 발생해야 합니다.
 * <p>
 * 각 테스트 메서드는 버그 수정 후 기대되는 동작(예외 발생)을 주석으로 포함하고 있습니다.
 */
@DisplayName("이슈 #3354: 재귀적 비교 시 컨테이너 내부의 존재하지 않는 필드")
class RecursiveComparison_NonExistentField_Test {

  // --- 1. 버그 재현을 위한 공통 데이터 클래스 정의 ---

  /** 'Player' 클래스는 'name' 필드만 가집니다. 'salary' 필드는 없습니다. */
  static class Player {
    String name;
    Player(String name) { this.name = name; }
  }

  /** Player 객체의 List를 필드로 가지는 팀 클래스 */
  static class TeamWithList {
    List<Player> players;
    TeamWithList(List<Player> players) { this.players = players; }
  }

  /** Player 객체의 Set을 필드로 가지는 팀 클래스 */
  static class TeamWithSet {
    Set<Player> players;
    TeamWithSet(Set<Player> players) { this.players = players; }
  }

  /** Player 객체의 배열을 필드로 가지는 팀 클래스 */
  static class TeamWithArray {
    Player[] players;
    TeamWithArray(Player[] players) { this.players = players; }
  }

  /** Optional<Player>를 필드로 가지는 팀 클래스 */
  static class TeamWithOptionalPlayer {
    Optional<Player> player;
    TeamWithOptionalPlayer(Optional<Player> player) { this.player = player; }
  }

  /** AtomicReference<Player>를 필드로 가지는 팀 클래스 */
  static class TeamWithAtomicReferencePlayer {
    AtomicReference<Player> player;
    TeamWithAtomicReferencePlayer(AtomicReference<Player> player) { this.player = player; }
  }


  // --- 2. 각 컨테이너 타입별 버그 재현 테스트 ---

  @Nested
  @DisplayName("Iterable (List/Set)에서 버그 재현")
  class ForIterable {
    @Test
    @DisplayName("List 내부 요소의 존재하지 않는 필드('salary')를 비교해도 예외가 발생하지 않는다")
    void shouldNotThrowExceptionWhenComparingNonExistentFieldInList() {
      // GIVEN
      TeamWithList team2022 = new TeamWithList(List.of(new Player("Son"), new Player("Kane")));
      TeamWithList team2023 = new TeamWithList(List.of(new Player("Maddison"), new Player("Romero")));

      // WHEN & THEN
      assertThatCode(() -> assertThat(team2022)
        .usingRecursiveComparison()
        .comparingOnlyFields("players.salary")
        .isEqualTo(team2023))
        .as("버그 재현: List의 존재하지 않는 필드를 비교했음에도 예외가 발생하지 않았습니다.")
        .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Set 내부 요소의 존재하지 않는 필드('salary')를 비교해도 예외가 발생하지 않는다")
    void shouldNotThrowExceptionWhenComparingNonExistentFieldInSet() {
      // GIVEN
      TeamWithSet team2022 = new TeamWithSet(Set.of(new Player("Son"), new Player("Kane")));
      TeamWithSet team2023 = new TeamWithSet(Set.of(new Player("Maddison"), new Player("Romero")));

      // WHEN & THEN
      assertThatCode(() -> assertThat(team2022)
        .usingRecursiveComparison()
        .comparingOnlyFields("players.salary")
        .isEqualTo(team2023))
        .as("버그 재현: Set의 존재하지 않는 필드를 비교했음에도 예외가 발생하지 않았습니다.")
        .doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("Array에서 버그 재현")
  class ForArray {
    @Test
    @DisplayName("배열 내부 요소의 존재하지 않는 필드('salary')를 비교해도 예외가 발생하지 않는다")
    void shouldNotThrowExceptionWhenComparingNonExistentFieldInArray() {
      // GIVEN
      TeamWithArray team2022 = new TeamWithArray(new Player[]{new Player("Son"), new Player("Kane")});
      TeamWithArray team2023 = new TeamWithArray(new Player[]{new Player("Maddison"), new Player("Romero")});

      // WHEN & THEN
      assertThatCode(() -> assertThat(team2022)
        .usingRecursiveComparison()
        .comparingOnlyFields("players.salary")
        .isEqualTo(team2023))
        .as("버그 재현: 배열의 존재하지 않는 필드를 비교했음에도 예외가 발생하지 않았습니다.")
        .doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("Optional에서 버그 재현")
  class ForOptional {
    @Test
    @DisplayName("Optional 내부 요소의 존재하지 않는 필드('salary')를 비교해도 예외가 발생하지 않는다")
    void shouldNotThrowExceptionWhenComparingNonExistentFieldInOptional() {
      // GIVEN
      TeamWithOptionalPlayer teamA = new TeamWithOptionalPlayer(Optional.of(new Player("Son")));
      TeamWithOptionalPlayer teamB = new TeamWithOptionalPlayer(Optional.of(new Player("Kane")));

      // WHEN & THEN
      assertThatCode(() -> assertThat(teamA)
        .usingRecursiveComparison()
        .comparingOnlyFields("player.salary") // 'player'는 Optional<Player> 타입
        .isEqualTo(teamB))
        .as("버그 재현: Optional의 존재하지 않는 필드를 비교했음에도 예외가 발생하지 않았습니다.")
        .doesNotThrowAnyException();

      /*
       * --- 버그 수정 후 기대되는 동작 ---
       * assertThatThrownBy(() -> assertThat(teamA)
       * .usingRecursiveComparison()
       * .comparingOnlyFields("player.salary")
       * .isEqualTo(teamB))
       * .isInstanceOf(IllegalArgumentException.class);
       */
    }
  }

  @Nested
  @DisplayName("AtomicReference에서 버그 재현")
  class ForAtomicReference {
    @Test
    @DisplayName("AtomicReference 내부 요소의 존재하지 않는 필드('salary')를 비교해도 예외가 발생하지 않는다")
    void shouldNotThrowExceptionWhenComparingNonExistentFieldInAtomicReference() {
      // GIVEN
      TeamWithAtomicReferencePlayer teamA = new TeamWithAtomicReferencePlayer(new AtomicReference<>(new Player("Son")));
      TeamWithAtomicReferencePlayer teamB = new TeamWithAtomicReferencePlayer(new AtomicReference<>(new Player("Kane")));

      // WHEN & THEN
      assertThatCode(() -> assertThat(teamA)
        .usingRecursiveComparison()
        .comparingOnlyFields("player.salary") // 'player'는 AtomicReference<Player> 타입
        .isEqualTo(teamB))
        .as("버그 재현: AtomicReference의 존재하지 않는 필드를 비교했음에도 예외가 발생하지 않았습니다.")
        .doesNotThrowAnyException();

      /*
       * --- 버그 수정 후 기대되는 동작 ---
       * assertThatThrownBy(() -> assertThat(teamA)
       * .usingRecursiveComparison()
       * .comparingOnlyFields("player.salary")
       * .isEqualTo(teamB))
       * .isInstanceOf(IllegalArgumentException.class);
       */
    }
  }
}
