package org.squirrelframework.foundation.fsm.cssparser;

import java.util.List;
import java.util.Map;

import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.Converter;
import org.squirrelframework.foundation.fsm.ConverterProvider;
import org.squirrelframework.foundation.fsm.HistoryType;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.cssparser.SimpleCssParser.ParserContext;
import org.squirrelframework.foundation.fsm.cssparser.SimpleCssParser.ParserState;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

import com.google.common.collect.Lists;

/**
 * This is an example on how to use state machine to build a simple CSS parser. The state machine was defined in fluent API.
 * 
 * @author Henry.He
 *
 */
public class SimpleCssParser extends AbstractStateMachine<SimpleCssParser, ParserState, Character, ParserContext>{
    
    enum ParserState {
        RULE, SELECTOR, PROPERTY, PROPERTY_NAME, PROPERTY_VALUE, COMMENT
    }
    
    static class ParserContext {
        private ParserContext() {}
        Character currChar;
        Character nextChar;
        List<CssRule> rules;
    }
    
    private static final Character STAR = '*';

    private static final Character SLASH = '/';

    private static final Character BRACKET_BEG = '{';

    private static final Character BRACKET_END = '}';

    private static final Character COLON = ':';

    private static final Character SEMI_COLON = ';';
    
    private StringBuilder buffer;
    
    private CssRule currRule;
    
    private CssProperty currProperty;
    
    private static final StateMachineBuilder<SimpleCssParser, ParserState, Character, ParserContext> builder;
    
    static {
        builder = StateMachineBuilderFactory.create(
                SimpleCssParser.class, ParserState.class, Character.class, ParserContext.class);
        builder.externalTransition().from(ParserState.RULE).to(ParserState.COMMENT).on(SLASH).when(
                new Condition<ParserContext>() {
                    @Override
                    public boolean isSatisfied(ParserContext context) {
                        return context.nextChar!=null && context.nextChar.equals(STAR);
                    }
                });
        
        builder.externalTransition().from(ParserState.COMMENT).to(ParserState.RULE).on(STAR).when(
                new Condition<ParserContext>() {
                    @Override
                    public boolean isSatisfied(ParserContext context) {
                        return context.nextChar!=null && context.nextChar.equals(SLASH);
                    }
                });
        builder.externalTransition().from(ParserState.SELECTOR).to(ParserState.PROPERTY).on(BRACKET_BEG);
        builder.externalTransition().from(ParserState.PROPERTY_NAME).to(ParserState.PROPERTY_VALUE).on(COLON);
        builder.externalTransition().from(ParserState.PROPERTY_VALUE).to(ParserState.PROPERTY_NAME).on(SEMI_COLON);
        builder.externalTransition().from(ParserState.PROPERTY).to(ParserState.SELECTOR).on(BRACKET_END).perform(
                new Action<SimpleCssParser, ParserState, Character, ParserContext>() {
            @Override
            public void execute(ParserState from, ParserState to, Character event,
                    ParserContext context, SimpleCssParser stateMachine) {
                stateMachine.setCurrRule(null);
                stateMachine.setCurrProperty(null);
            }
        });
        
        builder.defineSequentialStatesOn(ParserState.RULE, HistoryType.DEEP, 
                ParserState.SELECTOR, ParserState.PROPERTY);
        builder.defineSequentialStatesOn(ParserState.PROPERTY, HistoryType.DEEP, 
                ParserState.PROPERTY_NAME, ParserState.PROPERTY_VALUE);
        
        builder.onExit(ParserState.SELECTOR).perform(new Action<SimpleCssParser, ParserState, Character, ParserContext>() {
            @Override
            public void execute(ParserState from, ParserState to, Character event,
                    ParserContext context, SimpleCssParser stateMachine) {
                if(event.equals(SLASH)) return;
                
                CssRule rule = stateMachine.getCurrRule();
                if(rule==null) {
                    rule = new CssRule();
                    stateMachine.setCurrRule(rule);
                    context.rules.add(rule);
                }
                rule.setSelector(stateMachine.getBufferValue());
            }
        });
        
        builder.onExit(ParserState.PROPERTY_NAME).perform(new Action<SimpleCssParser, ParserState, Character, ParserContext>() {
            @Override
            public void execute(ParserState from, ParserState to, Character event,
                    ParserContext context, SimpleCssParser stateMachine) {
                if(event.equals(COLON)) {
                    CssProperty newProperty = new CssProperty();
                    newProperty.setName(stateMachine.getBufferValue());
                    stateMachine.getCurrRule().addProperty(newProperty);
                    stateMachine.setCurrProperty(newProperty);
                }
            }
        });
        
        builder.onExit(ParserState.PROPERTY_VALUE).perform(new Action<SimpleCssParser, ParserState, Character, ParserContext>() {
            @Override
            public void execute(ParserState from, ParserState to, Character event,
                    ParserContext context, SimpleCssParser stateMachine) {
                if(stateMachine.getCurrProperty()!=null && !event.equals(SLASH))
                    stateMachine.getCurrProperty().setValue(stateMachine.getBufferValue());
            }
        });
        
        ConverterProvider.INSTANCE.register(ParserState.class, new Converter.EnumConverter<ParserState>(ParserState.class));
        ConverterProvider.INSTANCE.register(Character.class, new Converter<Character>() {
            @Override
            public String convertToString(Character obj) {
                return obj.toString();
            }

            @Override
            public Character convertFromString(String name) {
                return name.charAt(0);
            }
        });
    }
    
    protected SimpleCssParser(
            ImmutableState<SimpleCssParser, ParserState, Character, ParserContext> initialState,
            Map<ParserState, ImmutableState<SimpleCssParser, ParserState, Character, ParserContext>> states) {
        super(initialState, states);
    }
    
    public String getBufferValue() {
        if(buffer==null) return "";
        String value = buffer.toString().trim();
        buffer = null;
        return value;
    }
    
    public CssRule getCurrRule() {
        return currRule;
    }

    public void setCurrRule(CssRule currRule) {
        this.currRule = currRule;
    }

    public CssProperty getCurrProperty() {
        return currProperty;
    }

    public void setCurrProperty(CssProperty currProperty) {
        this.currProperty = currProperty;
    }
    
    List<CssRule> parse(final String css) {
        List<CssRule> result = Lists.newArrayList();
        if(css==null || css.trim().isEmpty())
            return result;
        
        for (int i = 0; i < css.length(); i++) {
            Character c = css.charAt(i);
            ParserContext context = new ParserContext();
            context.rules = result;
            context.currChar = c;
            context.nextChar = (i<css.length()-1) ? css.charAt(i+1) : null;
            boolean isComment = getCurrentState()==ParserState.COMMENT;
            fire(c, context);
            if( (!isComment && getCurrentState()==ParserState.COMMENT) || 
                    (isComment && getCurrentState()!=ParserState.COMMENT) ) {
                i++;
            } 
        }
        return result;
    }
    
    @Override
    public void afterTransitionDeclined(ParserState sourceState, Character event, ParserContext context) {
        // record string values except comments
        if(sourceState!=ParserState.COMMENT) {
            if(buffer==null) 
                buffer=new StringBuilder();
            buffer.append(event);
        }
    }
    
    public static SimpleCssParser newParser() {
        return builder.newStateMachine(ParserState.RULE);
    }
}
