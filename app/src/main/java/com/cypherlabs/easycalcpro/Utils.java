package com.cypherlabs.easycalcpro;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Created by mghildiy on 11/19/2015.
 */
class Utils {
    private static final String TAG = "Utils";
    private static final char[] operands = {'0','1','2','3','4','5','6','7','8','9','.'};
    private static final char[] operators = {'+','-','*','/'};

    private static final BigDecimal billion = new BigDecimal("1000000000");
    private static final BigDecimal million = new BigDecimal("1000000");;
    private static final BigDecimal thousand = new BigDecimal("1000");
    private static final BigDecimal hundred = new BigDecimal("100");
    private static final BigDecimal crore = new BigDecimal("10000000");
    private static final BigDecimal lakh = new BigDecimal("100000");

    public static String convertNumberToWordsInEnglish(StringBuilder numberInString){
        BigDecimal number = new BigDecimal(numberInString.toString());
        BigDecimal[] billionsAndRemainder = number.divideAndRemainder(billion);//,new MathContext(15,RoundingMode.HALF_UP)
        BigDecimal[] millionsAndRemainder = billionsAndRemainder[1].divideAndRemainder(million);
        BigDecimal[] thousandsAndRemainder = millionsAndRemainder[1].divideAndRemainder(thousand);
        BigDecimal[] hundredsAndRemainder = thousandsAndRemainder[1].divideAndRemainder(hundred);

        StringBuilder result = new StringBuilder();
        if(billionsAndRemainder[0].compareTo(new BigDecimal("0"))!=0){
            result.append(billionsAndRemainder[0].setScale(0)).append(" billion ");
            //result.append(billionsAndRemainder[0]).append(" billion ");
        }
        if(millionsAndRemainder[0].compareTo(new BigDecimal("0"))!=0){
            result.append(millionsAndRemainder[0].setScale(0)).append(" million ");
            //result.append(millionsAndRemainder[0]).append(" million ");
        }
        if(thousandsAndRemainder[0].compareTo(new BigDecimal("0"))!=0){
            result.append(thousandsAndRemainder[0].setScale(0)).append(" thousand ");
            //result.append(thousandsAndRemainder[0]).append(" thousand ");
        }
        if(hundredsAndRemainder[0].compareTo(new BigDecimal("0"))!=0){
            result.append(hundredsAndRemainder[0].setScale(0)).append(" hundred ");
            //result.append(hundredsAndRemainder[0]).append(" hundred ");
        }
        return (result.append(hundredsAndRemainder[1]).toString());

    }

    public static String convertNumberToWordsInHindi(StringBuilder numberInString){
        BigDecimal number = new BigDecimal(numberInString.toString());
        BigDecimal[] croresAndRemainder = number.divideAndRemainder(crore);
        BigDecimal[] lakhsAndRemainder = croresAndRemainder[1].divideAndRemainder(lakh);
        BigDecimal[] hazarsAndRemainder = lakhsAndRemainder[1].divideAndRemainder(thousand);
        BigDecimal[] hundredsAndRemainder = hazarsAndRemainder[1].divideAndRemainder(hundred);

        StringBuilder result = new StringBuilder();
        if(croresAndRemainder[0].compareTo(new BigDecimal("0"))!=0){
            result.append(croresAndRemainder[0].setScale(0)).append(" crore ");
            //result.append(croresAndRemainder[0]).append(" crore ");
        }
        if(lakhsAndRemainder[0].compareTo(new BigDecimal("0"))!=0){
            result.append(lakhsAndRemainder[0].setScale(0)).append(" lakh ");
            //result.append(lakhsAndRemainder[0]).append(" lakh ");
        }
        if(hazarsAndRemainder[0].compareTo(new BigDecimal("0"))!=0){
            result.append(hazarsAndRemainder[0].setScale(0)).append(" hazaar ");
            //result.append(hazarsAndRemainder[0]).append(" hazaar ");
        }
        if(hundredsAndRemainder[0].compareTo(new BigDecimal("0"))!=0){
            result.append(hundredsAndRemainder[0].setScale(0)).append(" hundred ");
            //result.append(hundredsAndRemainder[0]).append(" hundred ");
        }
        return (result.append(hundredsAndRemainder[1]).toString());
    }


    public static boolean isCharacterAnOperand(char input){
        for(char operator:operands){
            if(input==operator){
               return true;
            }
        }
        return false;
    }

    public static boolean isCharacterAnOperator(char input){
        for(char operator:operators){
            if(input==operator){
                return true;
            }
        }
        return false;
    }

    public static void handleDivisonsAndultiplications(List<StringBuilder> components){
        for (int i = 0; i < components.size() - 1; i++) {
            if (components.get(i).toString().equals("/")) {
                BigDecimal dividend = new BigDecimal(components.get(i - 1).toString());
                BigDecimal divisor = new BigDecimal(components.get(i + 1).toString());
                BigDecimal quotient = dividend.divide(divisor, 2, RoundingMode.HALF_UP);
                components.remove(i - 1);
                components.remove(i - 1);
                components.remove(i - 1);
                components.add(i - 1, new StringBuilder(quotient.toString()));
                i = i - 1;
                handleDivisonsAndultiplications(components);
            }
            if (components.get(i).toString().equals("*")) {
                BigDecimal multiplicand = new BigDecimal(components.get(i - 1).toString());
                BigDecimal multiplier = new BigDecimal(components.get(i + 1).toString());
                BigDecimal product = multiplicand.multiply(multiplier);
                product = product.setScale(2, RoundingMode.HALF_UP);
                components.remove(i - 1);
                components.remove(i - 1);
                components.remove(i - 1);
                components.add(i - 1, new StringBuilder(product.toString()));
                i = i - 1;
                handleDivisonsAndultiplications(components);
            }
        }
    }

    public static void handleAdditionsAndSubtractions(List<StringBuilder> components){
        for (int i = 0; i < components.size() - 1; i++) {
            if (components.get(i).toString().equals("+")) {
                BigDecimal augend = new BigDecimal(components.get(i - 1).toString());
                BigDecimal addend = new BigDecimal(components.get(i + 1).toString());
                BigDecimal sum = augend.add(addend);
                sum.setScale(2, RoundingMode.HALF_UP);
                components.remove(i - 1);
                components.remove(i - 1);
                components.remove(i - 1);
                components.add(i - 1, new StringBuilder(sum.toString()));
                i = i - 1;
                handleAdditionsAndSubtractions(components);
            }
            if (components.get(i).toString().equals("-")) {
                BigDecimal minuend = new BigDecimal(components.get(i - 1).toString());
                BigDecimal subtrahend = new BigDecimal(components.get(i + 1).toString());
                BigDecimal difference = minuend.subtract(subtrahend);
                difference.setScale(2, RoundingMode.HALF_UP);
                components.remove(i - 1);
                components.remove(i - 1);
                components.remove(i - 1);
                components.add(i - 1, new StringBuilder(difference.toString()));
                i = i - 1;
                handleAdditionsAndSubtractions(components);
            }
        }
    }

    public static void solver(List<String> expressionComponents) {
        BigDecimal operandFirst = null;
        BigDecimal operandSecond = null;
        BigDecimal result = null;
        while (expressionComponents.contains("/") || expressionComponents.contains("*")) {
            int indexOfDiv = expressionComponents.indexOf("/");
            int indexOfMul = expressionComponents.indexOf("*");
            boolean isDivison = false;
            if (indexOfDiv > -1 && indexOfMul > -1) {
                if (indexOfDiv < indexOfMul) {
                    isDivison = true;
                    operandFirst = new BigDecimal(expressionComponents.get(indexOfDiv - 1));
                    operandSecond = new BigDecimal(expressionComponents.get(indexOfDiv + 1));
                }else{
                    isDivison = false;
                    operandFirst = new BigDecimal(expressionComponents.get(indexOfMul - 1));
                    operandSecond = new BigDecimal(expressionComponents.get(indexOfMul + 1));
                }
            }else{
                if(indexOfDiv > -1){
                    isDivison = true;
                    operandFirst = new BigDecimal(expressionComponents.get(indexOfDiv - 1));
                    operandSecond = new BigDecimal(expressionComponents.get(indexOfDiv + 1));
                }else{
                    isDivison = false;
                    operandFirst = new BigDecimal(expressionComponents.get(indexOfMul - 1));
                    operandSecond = new BigDecimal(expressionComponents.get(indexOfMul + 1));
                }
            }
            if(isDivison){
                result = operandFirst.divide(operandSecond, 2, RoundingMode.HALF_UP);
                expressionComponents.subList(indexOfDiv - 1, indexOfDiv + 2).clear();
                expressionComponents.add(indexOfDiv - 1, result.toString());
            }else{
                result = operandFirst.multiply(operandSecond);
                result = result.setScale(2, RoundingMode.HALF_UP);
                expressionComponents.subList(indexOfMul - 1, indexOfMul + 2).clear();
                expressionComponents.add(indexOfMul - 1, result.toString());
            }
        }

        while (expressionComponents.contains("+") || expressionComponents.contains("-")) {
            int indexOfAdd = expressionComponents.indexOf("+");
            int indexOfSub = expressionComponents.indexOf("-");
            boolean isAddition = false;
            if (indexOfAdd > -1 && indexOfSub > -1) {
                if (indexOfAdd < indexOfSub) {
                    isAddition = true;
                    operandFirst = new BigDecimal(expressionComponents.get(indexOfAdd - 1));
                    operandSecond = new BigDecimal(expressionComponents.get(indexOfAdd + 1));
                }else{
                    isAddition = false;
                    operandFirst = new BigDecimal(expressionComponents.get(indexOfSub - 1));
                    operandSecond = new BigDecimal(expressionComponents.get(indexOfSub + 1));
                }
            }else{
                if(indexOfAdd > -1){
                    isAddition = true;
                    operandFirst = new BigDecimal(expressionComponents.get(indexOfAdd - 1));
                    operandSecond = new BigDecimal(expressionComponents.get(indexOfAdd + 1));
                }else{
                    isAddition = false;
                    operandFirst = new BigDecimal(expressionComponents.get(indexOfSub - 1));
                    operandSecond = new BigDecimal(expressionComponents.get(indexOfSub + 1));
                }
            }
            if(isAddition){
                result = operandFirst.add(operandSecond);
                //result.setScale(2, RoundingMode.HALF_UP);
                result = result.setScale(2, RoundingMode.HALF_UP);
                expressionComponents.subList(indexOfAdd - 1, indexOfAdd + 2).clear();
                expressionComponents.add(indexOfAdd - 1, result.toString());
            }else{
                result = operandFirst.subtract(operandSecond);
                //result.setScale(2, RoundingMode.HALF_UP);
                result = result.setScale(2, RoundingMode.HALF_UP);
                expressionComponents.subList(indexOfSub - 1, indexOfSub + 2).clear();
                expressionComponents.add(indexOfSub - 1, result.toString());
            }

        }
    }
}
