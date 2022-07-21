package com.example.javabot.service;

import com.example.javabot.config.BotConfig;
import com.example.javabot.model.User;
import com.example.javabot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    final BotConfig config;

    static final String HELP_TEXT = "This bot is just a bot ) He knows some jokes and memes.\n He's become to fun _)\n" +
            "Type /start to see a welcome message\n" +
            "Type /mydata to see your profile data\n" +
            "Type /help to read main info";

    public TelegramBot(BotConfig config){
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start","get a hello message"));
        listOfCommands.add(new BotCommand("/mydata","get your data stored"));
        listOfCommands.add(new BotCommand("/deletedata","delete my data"));
        listOfCommands.add(new BotCommand("/help","how to use this bot"));
        listOfCommands.add(new BotCommand("/settings","set your preferences"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(),null));
        } catch (TelegramApiException e){
            log.error("Error setting command list: " + e.getMessage());
        }

    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatID = update.getMessage().getChatId();

            switch (messageText){
                case "/start":

                    registerUser(update.getMessage());

                    startCommandReceived(chatID, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatID,HELP_TEXT);
                    break;
                default:
                    sendMessage(chatID, "Sorry, not sorry =(");
            }
        }
    }

    private void startCommandReceived(Long chatID, String name) {
        String answer = "Hi, " + name + ", nice to meet you !)";
        log.info("Replied to user " + name);

        sendMessage(chatID, answer);
    }

    private void sendMessage(long chatID, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatID));
        message.setText(textToSend);
        try { execute(message); }
        catch (TelegramApiException e){
            log.error("Error occurred:" + e.getMessage());
        }

    }

    private void registerUser(Message message){
        if(userRepository.findById(message.getChatId()).isEmpty()){

            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved " + user);
        }
    }

}
