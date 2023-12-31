from flask import render_template, flash, redirect, jsonify, url_for, request, g
from flask_login import current_user, login_user, logout_user, login_required
from werkzeug.urls import url_parse
from app import app, db
from app.forms import LoginForm, RegistrationForm
from app.models import User, VocabRecord
from googletrans import Translator
from app.api.auth import token_auth


translator = Translator()


def translate(text):
    return translator.translate(text, dest='ru').text


@app.route('/')
@app.route('/index')
@login_required
def index():
    text = "Superfluidity is the characteristic property of a fluid with zero viscosity which therefore flows without loss of kinetic energy. When stirred, a superfluid forms cellular vortices that continue to rotate indefinitely. Superfluidity occurs in two isotopes of helium (helium-3 and helium-4) when they are liquefied by cooling to cryogenic temperatures. It is also a property of various other exotic states of matter theorized to exist in astrophysics, high-energy physics, and theories of quantum gravity.[1] The phenomenon is related to Bose–Einstein condensation, but neither is a specific type of the other: not all Bose-Einstein condensates can be regarded as superfluids, and not all superfluids are Bose–Einstein condensates.[2] The theory of superfluidity was developed by Lev Landau. Superfluidity was originally discovered in liquid helium, by Pyotr Kapitsa and John F. Allen. It has since been described through phenomenology and microscopic theories. In liquid helium-4, the superfluidity occurs at far higher temperatures than it does in helium-3. Each atom of helium-4 is a boson particle, by virtue of its integer spin. A helium-3 atom is a fermion particle; it can form bosons only by pairing with itself at much lower temperatures. The discovery of superfluidity in helium-3 was the basis for the award of the 1996 Nobel Prize in Physics.[1] This process is similar to the electron pairing in superconductivity."

    username = current_user.username

    # Sentence translation.
    translated = translate(text)
    text_sentences = text.split('.')
    translated_sentences = translated.split('.')
    res = [{"text_sentence": x + '.'} for x in text_sentences]
    for i in range(len(res)):
        res[i]['translated_sentence'] = translated_sentences[i]

    # Words highlightning.
    user = User.query.filter_by(username=username).first()
    wordlist = user.words.all()
    words_to_highlight = [x.word for x in wordlist]
    words_to_highlight = [x.lower() for x in words_to_highlight]
    for i in range(len(res)):
        sentence_words = res[i]['text_sentence'].split()
        highlights = [0] * len(sentence_words)
        for j in range(len(sentence_words)):
            for w in words_to_highlight:
                if w == sentence_words[j].lower():
                    highlights[j] = 1
        res[i]['sentence_words'] = list(zip(sentence_words, highlights))

    vocab = prepare_vocab()

    return render_template('index.html', title='Home', res=res, vocab=vocab)


@app.route('/register', methods=['GET', 'POST'])
def register():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    form = RegistrationForm()
    if form.validate_on_submit():
        username = form.username.data
        user = User(username=username, email=form.email.data)
        user.set_password(form.password.data)
        db.session.add(user)
        db.session.commit()
        flash('Congratulations, you are now a registered user!')
        return redirect(url_for('login'))
    return render_template('register.html', title='Register', form=form)


@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    form = LoginForm()
    if form.validate_on_submit():
        user = User.query.filter_by(username=form.username.data).first()
        if user is None or not user.check_password(form.password.data):
            flash('Invalid username or password')
            return redirect(url_for('login'))
        login_user(user, remember=form.remember_me.data)
        next_page = request.args.get('next')
        if not next_page or url_parse(next_page).netloc != '':
            next_page = url_for('index')
        return redirect(next_page)
    return render_template('login.html', title='Sign In', form=form)


@app.route('/logout')
def logout():
    logout_user()
    return redirect(url_for('index'))


# API Section.

@app.route('/api/add/<word>', methods=['GET', 'POST'])
@token_auth.login_required
def api_add(word):
    username = g.current_user.username
    user = User.query.filter_by(username=username).first()

    if word[0].isupper() == False:
        word = word.capitalize()
    translation = translate(word)
    vocab_record = VocabRecord(word=word, translation=translation, author=user)
    db.session.add(vocab_record)
    db.session.commit()
    return word


@app.route('/api/remove/<word>', methods=['GET', 'POST'])
@token_auth.login_required
def api_remove(word):
    username = g.current_user.username
    user = User.query.filter_by(username=username).first()

    if word[0].isupper() == False:
        word = word.capitalize()
    vocab_record = VocabRecord.query.filter_by(word=word, author=user)
    for i in vocab_record:
        db.session.delete(i)
    db.session.commit()
    return word


@app.route('/api/wordlist', methods=['GET', 'POST'])
@token_auth.login_required
def api_wordlist():
    username = g.current_user.username
    res = prepare_vocab(username=username)
    res = dict(res)
    res = jsonify(res)
    return res


# Reading Section.

@app.route('/reading/add/<word>', methods=['GET', 'POST'])
@login_required
def reading_add(word):
    username = current_user.username
    user = User.query.filter_by(username=username).first()

    if word[0].isupper() == False:
        word = word.capitalize()
    translation = translate(word)
    vocab_record = VocabRecord(word=word, translation=translation, author=user)
    db.session.add(vocab_record)
    db.session.commit()
    return redirect(url_for("index"))


@app.route('/reading/remove/<word>', methods=['GET', 'POST'])
@login_required
def reading_remove(word):
    username = current_user.username
    user = User.query.filter_by(username=username).first()

    if word[0].isupper() == False:
        word = word.capitalize()
    vocab_record = VocabRecord.query.filter_by(word=word, author=user)
    for i in vocab_record:
        db.session.delete(i)
    db.session.commit()
    return redirect(url_for("index"))


# Web Section.

@app.route('/wordlist', methods=['GET', 'POST'])
@login_required
def web_wordlist():
    vocab = prepare_vocab()
    return render_template('wordlist_page.html', title='List of Words', vocab=vocab)


@app.route('/remove/<word>', methods=['GET', 'POST'])
@login_required
def web_remove(word):
    username = current_user.username
    user = User.query.filter_by(username=username).first()

    if word[0].isupper() == False:
        word = word.capitalize()
    vocab_record = VocabRecord.query.filter_by(word=word, author=user)
    for i in vocab_record:
        db.session.delete(i)
    db.session.commit()

    web_wordlist()
    return redirect(url_for("web_wordlist"))


# Utils.

def prepare_vocab(username=None):
    if username is None:
        username = current_user.username
    user = User.query.filter_by(username=username).first()
    wordlist = user.words.all()

    if len(wordlist) == 0:
        res = None
    else:
        res = [(x.word, x.translation) for x in wordlist]
        res = sorted(res, key=lambda x: x[0])

    return res
