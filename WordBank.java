import java.util.Random;

public class WordBank {
    private String[][] words = {
        {"TIGER", "A wild animal with orange and black stripes"},
        {"APPLE", "A sweet red or green fruit "},
        {"ELEPHANT", "A large grey animal with a trunk and big ears"},
        {"PIZZA", "A round baked Italian food with cheese and toppings"},
        {"GUITAR", "A musical instrument with strings that you strum"},
        {"DOCTOR", "A person who helps sick people get better"},
        {"AIRPLANE", "A large vehicle with wings that flies in the sky"},
        {"OCTOPUS", "A sea animal with eight arms and a soft body"},
        {"RAINBOW", "Colorful arch that appears in the sky after rain"},
        {"LIBRARY", "A quiet place with many books to read and borrow"},
        {"CAT", "A small furry pet that meows and catches mice"},
        {"DOG", "A loyal furry pet that barks and wags its tail"},
        {"BANANA", "A sweet long yellow fruit that monkeys love"},
        {"WATER", "Clear liquid we drink when we are thirsty"},
        {"SCHOOL", "A place where students go to study and learn"},
        {"MILK", "A healthy white drink that comes from cows"},
        {"SUN", "Bright and hot star that shines during the day"},
        {"FISH", "An animal with fins that lives and swims in water"},
        {"BOOK", "Something you read with paper pages and a cover"},
        {"BALL", "A round toy used for kicking, throwing, or catching"}
    };

    public String[] getRandomWord() {
        Random random = new Random();
        return words[random.nextInt(words.length)];
    }
}
