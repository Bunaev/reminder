<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password','password-confirm','email','user.attributes.telegram') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        Регистрация
    <#elseif section = "form">
        <form id="kc-register-form" class="${properties.kcFormClass!}" action="${url.registrationAction}" method="post" onsubmit="return validateForm()">
            
            <#-- 1. Имя пользователя -->
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="username" class="${properties.kcLabelClass!}">Имя пользователя</label>
                    <span class="required">*</span>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="text" id="username" class="${properties.kcInputClass!}" name="username" 
                           value="${(register.formData.username!'')}"
                           required
                           minlength="3"
                           maxlength="20"
                           pattern="[a-zA-Z0-9_]+"
                           title="Только латинские буквы, цифры и знак подчеркивания"
                           aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"
                           autofocus/>
                    
                    <span class="error-message" id="usernameError" style="color: #f44336; font-size: 13px; display: none;"></span>
                    
                    <#-- ⚠️ ОШИБКА ОТ KEYCLOAK (уникальность, формат и т.д.) -->
                    <#if messagesPerField.existsError('username')>
                        <span id="input-error-username" class="${properties.kcInputErrorMessageClass!}" style="color: #f44336; font-size: 13px; display: block; margin-top: 5px;">
                            ${kcSanitize(messagesPerField.get('username'))?no_esc}
                        </span>
                    </#if>
                </div>
            </div>

            <#-- 2. Пароль -->
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="password" class="${properties.kcLabelClass!}">Пароль</label>
                    <span class="required">*</span>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="password" id="password" class="${properties.kcInputClass!}" name="password"
                           required
                           minlength="6"
                           autocomplete="new-password"/>
                    <span class="error-message" id="passwordError" style="color: #f44336; font-size: 13px; display: none;"></span>
                    <div id="passwordRequirements" style="font-size: 12px; margin-top: 5px;">
                        <span id="reqLength" style="display: block;">✗ Минимум 6 символов</span>
                        <span id="reqDigit" style="display: block;">✗ Хотя бы одна цифра</span>
                        <span id="reqLower" style="display: block;">✗ Хотя бы одна строчная буква</span>
                        <span id="reqUpper" style="display: block;">✗ Хотя бы одна заглавная буква</span>
                    </div>
                </div>
            </div>

            <#-- 3. Подтверждение пароля -->
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="password-confirm" class="${properties.kcLabelClass!}">Подтверждение пароля</label>
                    <span class="required">*</span>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="password" id="password-confirm" class="${properties.kcInputClass!}" name="password-confirm"
                           required
                           autocomplete="new-password"/>
                    <span class="error-message" id="passwordConfirmError" style="color: #f44336; font-size: 13px; display: none;"></span>
                </div>
            </div>

            <#-- 4. Email -->
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="email" class="${properties.kcLabelClass!}">Email</label>
                    <span class="required">*</span>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="email" id="email" class="${properties.kcInputClass!}" name="email" 
                           value="${(register.formData.email!'')}"
                           required
                           pattern="[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$"
                           title="Введите корректный email"/>
                    <span class="error-message" id="emailError" style="color: #f44336; font-size: 13px; display: none;"></span>
                </div>
            </div>

            <#-- 5. Telegram -->
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="user.attributes.telegram" class="${properties.kcLabelClass!}">Telegram</label>
                    <span class="required">*</span>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="text" id="user.attributes.telegram" class="${properties.kcInputClass!}" 
                           name="user.attributes.telegram" 
                           value="${(register.formData['user.attributes.telegram']!'')}"
                           required
                           pattern="@[a-zA-Z0-9_]+"
                           title="@username"/>
                    <span class="error-message" id="telegramError" style="color: #f44336; font-size: 13px; display: none;"></span>
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                        <span><a href="${url.loginUrl}">« Назад ко входу</a></span>
                    </div>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" 
                           type="submit" value="Зарегистрироваться"/>
                </div>
            </div>
        </form>

        <script>
            const username = document.getElementById('username');
            const password = document.getElementById('password');
            const passwordConfirm = document.getElementById('password-confirm');
            const email = document.getElementById('email');
            const telegram = document.getElementById('user.attributes.telegram');

            // Валидация имени пользователя
            username.addEventListener('input', function() {
                const error = document.getElementById('usernameError');
                const pattern = /^[a-zA-Z0-9_]+$/;
                
                if (this.value.length < 3) {
                    error.textContent = 'Минимум 3 символа';
                    error.style.display = 'block';
                } else if (!pattern.test(this.value)) {
                    error.textContent = 'Только латинские буквы, цифры и _';
                    error.style.display = 'block';
                } else {
                    error.style.display = 'none';
                }
            });

            // Валидация пароля
            password.addEventListener('input', function() {
                const pwd = this.value;
                
                document.getElementById('reqLength').innerHTML = (pwd.length >= 6 ? '✓' : '✗') + ' Минимум 6 символов';
                document.getElementById('reqLength').style.color = pwd.length >= 6 ? '#4CAF50' : '#f44336';
                
                const hasDigit = /[0-9]/.test(pwd);
                document.getElementById('reqDigit').innerHTML = (hasDigit ? '✓' : '✗') + ' Хотя бы одна цифра';
                document.getElementById('reqDigit').style.color = hasDigit ? '#4CAF50' : '#f44336';
                
                const hasLower = /[a-z]/.test(pwd);
                document.getElementById('reqLower').innerHTML = (hasLower ? '✓' : '✗') + ' Хотя бы одна строчная буква';
                document.getElementById('reqLower').style.color = hasLower ? '#4CAF50' : '#f44336';
                
                const hasUpper = /[A-Z]/.test(pwd);
                document.getElementById('reqUpper').innerHTML = (hasUpper ? '✓' : '✗') + ' Хотя бы одна заглавная буква';
                document.getElementById('reqUpper').style.color = hasUpper ? '#4CAF50' : '#f44336';
            });

            // Валидация подтверждения пароля
            passwordConfirm.addEventListener('input', function() {
                const error = document.getElementById('passwordConfirmError');
                if (this.value !== password.value) {
                    error.textContent = 'Пароли не совпадают';
                    error.style.display = 'block';
                } else {
                    error.style.display = 'none';
                }
            });

            // Валидация email
            email.addEventListener('input', function() {
                const error = document.getElementById('emailError');
                const pattern = /^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$/i;
                
                if (!pattern.test(this.value)) {
                    error.textContent = 'Введите корректный email';
                    error.style.display = 'block';
                } else {
                    error.style.display = 'none';
                }
            });

            // Валидация Telegram
            telegram.addEventListener('input', function() {
                const error = document.getElementById('telegramError');
                const pattern = /^@[a-zA-Z0-9_]+$/;
                
                if (!pattern.test(this.value)) {
                    error.textContent = 'Должен начинаться с @, только латиница, цифры, _';
                    error.style.display = 'block';
                } else {
                    error.style.display = 'none';
                }
            });

            // Финальная проверка перед отправкой
            function validateForm() {
                let isValid = true;
                
                // Проверка username
                if (username.value.length < 3 || !/^[a-zA-Z0-9_]+$/.test(username.value)) {
                    isValid = false;
                }
                
                // Проверка пароля
                const pwd = password.value;
                if (pwd.length < 6 || !/[0-9]/.test(pwd) || !/[a-z]/.test(pwd) || !/[A-Z]/.test(pwd)) {
                    isValid = false;
                }
                
                // Проверка совпадения паролей
                if (password.value !== passwordConfirm.value) {
                    isValid = false;
                }
                
                // Проверка email
                if (!/^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$/i.test(email.value)) {
                    isValid = false;
                }
                
                // Проверка Telegram
                if (!/^@[a-zA-Z0-9_]+$/.test(telegram.value)) {
                    isValid = false;
                }
                
                return isValid;
            }
        </script>
    </#if>
</@layout.registrationLayout>