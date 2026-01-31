package com.lmstudio.chat

import com.lmstudio.chat.terminal.Command
import com.lmstudio.chat.terminal.CommandProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for CommandProcessor.
 */
class CommandProcessorTest {

    private val processor = CommandProcessor()

    @Test
    fun `test chat message parsing`() {
        val result = processor.parse("Hello, how are you?")
        assertIs<Command.Chat>(result)
        assertEquals("Hello, how are you?", result.message)
    }

    @Test
    fun `test help command`() {
        val result = processor.parse("/help")
        assertIs<Command.Help>(result)
    }

    @Test
    fun `test clear command`() {
        val result = processor.parse("/clear")
        assertIs<Command.Clear>(result)
    }

    @Test
    fun `test exit command`() {
        assertIs<Command.Exit>(processor.parse("/exit"))
        assertIs<Command.Exit>(processor.parse("/quit"))
    }

    @Test
    fun `test save command with filename`() {
        val result = processor.parse("/save my-conversation")
        assertIs<Command.Save>(result)
        assertEquals("my-conversation", result.filename)
    }

    @Test
    fun `test save command without filename`() {
        val result = processor.parse("/save")
        assertIs<Command.Error>(result)
    }

    @Test
    fun `test load command with filename`() {
        val result = processor.parse("/load my-conversation")
        assertIs<Command.Load>(result)
        assertEquals("my-conversation", result.filename)
    }

    @Test
    fun `test model command`() {
        val result = processor.parse("/model llama-3.2-1b")
        assertIs<Command.SwitchModel>(result)
        assertEquals("llama-3.2-1b", result.modelName)
    }

    @Test
    fun `test models command`() {
        val result = processor.parse("/models")
        assertIs<Command.ListModels>(result)
    }

    @Test
    fun `test config command`() {
        val result = processor.parse("/config")
        assertIs<Command.ShowConfig>(result)
    }

    @Test
    fun `test system command`() {
        val result = processor.parse("/system You are a helpful assistant")
        assertIs<Command.SetSystemPrompt>(result)
        assertEquals("You are a helpful assistant", result.prompt)
    }

    @Test
    fun `test set temperature command`() {
        val result = processor.parse("/set temperature 0.8")
        assertIs<Command.SetParameter>(result)
        assertEquals("temperature", result.name)
        assertEquals(0.8, result.value)
    }

    @Test
    fun `test set max_tokens command`() {
        val result = processor.parse("/set max_tokens 1000")
        assertIs<Command.SetParameter>(result)
        assertEquals("max_tokens", result.name)
        assertEquals(1000, result.value)
    }

    @Test
    fun `test set invalid temperature`() {
        val result = processor.parse("/set temperature 5.0")
        assertIs<Command.Error>(result)
    }

    @Test
    fun `test history command`() {
        val result = processor.parse("/history")
        assertIs<Command.ShowHistory>(result)
    }

    @Test
    fun `test tokens command`() {
        val result = processor.parse("/tokens")
        assertIs<Command.ShowTokens>(result)
    }

    @Test
    fun `test unknown command`() {
        val result = processor.parse("/unknown")
        assertIs<Command.Unknown>(result)
        assertEquals("/unknown", result.command)
    }

    @Test
    fun `test case insensitivity`() {
        assertIs<Command.Help>(processor.parse("/HELP"))
        assertIs<Command.Clear>(processor.parse("/CLEAR"))
        assertIs<Command.Exit>(processor.parse("/EXIT"))
    }
}
