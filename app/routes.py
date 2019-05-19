from flask import render_template, flash, redirect, jsonify, url_for
from app import app
from app.forms import LoginForm
from googletrans import Translator


class UserData:
    def __init__(self):
        self.wordlist = list()
        self.translations = dict()
        self.translator = Translator()

    def add_word(self, word):
        if word[0].isupper() == False:
            word = word.capitalize()

        if word not in self.wordlist:
            self.wordlist.append(word)
            self.translations[word] = self.translate(word)

    def remove_word(self, word):
        if word[0].isupper() == False:
            word = word.capitalize()

        if word in self.wordlist:
            self.wordlist.remove(word)

    def translate(self, text):
        res = self.translator.translate(text, dest='ru').text
        return res


users = dict(test=UserData())
username = 'test'
users[username].add_word("one")
users[username].add_word("superfluidity")


@app.route('/')
@app.route('/index')
def index():
    text = "Superfluidity is the characteristic property of a fluid with zero viscosity which therefore flows without loss of kinetic energy. When stirred, a superfluid forms cellular vortices that continue to rotate indefinitely. Superfluidity occurs in two isotopes of helium (helium-3 and helium-4) when they are liquefied by cooling to cryogenic temperatures. It is also a property of various other exotic states of matter theorized to exist in astrophysics, high-energy physics, and theories of quantum gravity.[1] The phenomenon is related to Bose–Einstein condensation, but neither is a specific type of the other: not all Bose-Einstein condensates can be regarded as superfluids, and not all superfluids are Bose–Einstein condensates.[2] The theory of superfluidity was developed by Lev Landau."
    translated = users[username].translate(text)

    text_sentences = text.split('.')
    translated_sentences = translated.split('.')

    # Sentence translation.
    res = [{"text_sentence": x + '.'} for x in text_sentences]
    for i in range(len(res)):
        res[i]['translated_sentence'] = translated_sentences[i]

    # Words highlightning.
    words_to_highlight = users[username].wordlist
    words_to_highlight = [x.lower() for x in words_to_highlight]
    for i in range(len(res)):
        sentence_words = res[i]['text_sentence'].split()
        highlights = [0] * len(sentence_words)
        for j in range(len(sentence_words)):
            for w in words_to_highlight:
                if w == sentence_words[j].lower():
                    highlights[j] = 1
        res[i]['sentence_words'] = list(zip(sentence_words, highlights))

    wordlist = prepare_wordlist()
    translations = [users[username].translations[x] for x in wordlist]

    return render_template('index.html', title='Home', res=res, wordlist_translations=zip(wordlist, translations))


@app.route('/login', methods=['GET', 'POST'])
def login():
    form = LoginForm()
    if form.validate_on_submit():
        flash('Login requested for user {}, remember_me={}'.format(
            form.username.data, form.remember_me.data))
        return redirect('/index')
    return render_template('login.html', title='Sign In', form=form)


# API Section.

@app.route('/api/add/<word>', methods=['GET', 'POST'])
def api_add(word):
    users[username].add_word(word)
    return word


@app.route('/api/remove/<word>', methods=['GET', 'POST'])
def api_remove(word):
    users[username].remove_word(word)
    return word


@app.route('/api/wordlist', methods=['GET', 'POST'])
def api_wordlist():
    wordlist = users[username].wordlist
    translations = [users[username].translations[x].capitalize for x in wordlist]
    res = jsonify(dict(zip(wordlist, translations)))
    return res


# Reading Section.

@app.route('/reading/add/<word>', methods=['GET', 'POST'])
def reading_add(word):
    users[username].add_word(word)
    return redirect(url_for("index"))


@app.route('/reading/remove/<word>', methods=['GET', 'POST'])
def reading_remove(word):
    users[username].remove_word(word)
    return redirect(url_for("index"))


# Web Section.

@app.route('/wordlist', methods=['GET', 'POST'])
def web_wordlist():
    wordlist = prepare_wordlist()
    translations = [users[username].translations[x.lower()] for x in wordlist]
    return render_template('wordlist_page.html', title='List of Words', wordlist_translations=zip(wordlist, translations))


@app.route('/remove/<word>', methods=['GET', 'POST'])
def web_remove(word):
    api_remove(word)
    web_wordlist()
    return redirect(url_for("web_wordlist"))


# Utils.

def prepare_wordlist():
    wordlist = users[username].wordlist
    wordlist = sorted(wordlist)

    if len(wordlist) == 0:
        wordlist = None

    return wordlist
