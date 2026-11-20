package pl.masslany.podkop.common.models.survey

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.business.common.domain.models.common.Actions
import pl.masslany.podkop.business.common.domain.models.common.Answer
import pl.masslany.podkop.business.common.domain.models.common.Survey

class SurveyStateMapperTest {

    @Test
    fun `to survey state reads voted option number and locks voting`() {
        val state = survey(
            answers = listOf(
                Answer(count = 3, id = 11, text = "One", voted = 0),
                Answer(count = 7, id = 22, text = "Two", voted = 0),
            ),
            voted = 2,
        ).toSurveyState()

        val actual = assertNotNull(state)

        assertEquals(2, actual.votedOptionNumber)
        assertTrue(actual.hasVoted)
        assertFalse(actual.isVoteActionEnabled)
        assertTrue(actual.areResultsVisibleByDefault)
        assertEquals(listOf(false, true), actual.answers.map { it.isSelected })
    }

    @Test
    fun `to survey state prefers answer vote marker over top level voted value`() {
        val state = survey(
            answers = listOf(
                Answer(count = 3, id = 11, text = "One", voted = 0),
                Answer(count = 7, id = 22, text = "Two", voted = 0),
                Answer(count = 5, id = 33, text = "Three", voted = 1),
            ),
            voted = 1,
        ).toSurveyState()

        val actual = assertNotNull(state)

        assertEquals(3, actual.votedOptionNumber)
        assertEquals(listOf(false, false, true), actual.answers.map { it.isSelected })
    }

    @Test
    fun `to survey state enables voting from survey vote action`() {
        val state = survey(
            answers = listOf(
                Answer(count = 3, id = 11, text = "One", voted = 0),
                Answer(count = 7, id = 22, text = "Two", voted = 0),
            ),
            voted = 0,
            canVote = true,
        ).toSurveyState()

        val actual = assertNotNull(state)

        assertTrue(actual.canVote)
        assertTrue(actual.isVoteActionEnabled)
        assertFalse(actual.hasVoted)
    }

    @Test
    fun `register vote increments selected answer and locks survey`() {
        val actual = SurveyState(
            question = "Pick one",
            answers = persistentListOf(
                AnswerState(
                    isSelected = false,
                    text = "One",
                    count = 2,
                    percentageFraction = 0.4f,
                    percentage = "40",
                ),
                AnswerState(
                    isSelected = false,
                    text = "Two",
                    count = 3,
                    percentageFraction = 0.6f,
                    percentage = "60",
                ),
            ),
            count = 5,
            canVote = true,
        ).registerVote(optionNumber = 1)

        assertEquals(6, actual.count)
        assertEquals(1, actual.votedOptionNumber)
        assertFalse(actual.isVoteActionEnabled)
        assertFalse(actual.isResultsToggleVisible)
        assertTrue(actual.answers[0].isSelected)
        assertEquals(3, actual.answers[0].count)
        assertEquals("50", actual.answers[0].percentage)
        assertFalse(actual.answers[1].isSelected)
        assertEquals(3, actual.answers[1].count)
        assertEquals("50", actual.answers[1].percentage)
    }

    private fun survey(
        answers: List<Answer>,
        voted: Int,
        canVote: Boolean = false,
    ): Survey = Survey(
        actions = Actions(
            create = false,
            createFavourite = false,
            delete = false,
            deleteFavourite = false,
            finishAma = false,
            report = false,
            startAma = false,
            undoVote = false,
            update = false,
            voteDown = false,
            voteUp = false,
            vote = canVote,
        ),
        answers = answers,
        count = answers.sumOf { it.count },
        deletable = false,
        editable = false,
        key = "survey",
        question = "Pick one",
        voted = voted,
    )
}
